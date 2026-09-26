package org.frias.avalon.domain.product.application.usecase.propagate;

import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.core.tenant.TenantContext;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.outlet.domain.model.OutletDomain;
import org.frias.avalon.domain.outlet.domain.port.OutletRepositoryPort;
import org.frias.avalon.domain.product.domain.BarcodeDomain;
import org.frias.avalon.domain.product.domain.repository.BarcodeRepositoryPort;
import org.frias.avalon.domain.product.infraestructure.entity.Product;
import org.frias.avalon.domain.product.infraestructure.entity.ProductOutlet;
import org.frias.avalon.domain.product.infraestructure.repository.JpaGlobalProductRepository;
import org.frias.avalon.domain.product.infraestructure.repository.JpaProductOutletRepository;
import org.frias.avalon.domain.product.infrastructure.entity.ProductCompanyEntity;
import org.frias.avalon.domain.product.infrastructure.repository.JpaProductCompanyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Caso de uso para propagar en cascada un producto corporativo (Nivel 2)
 * a todas las tiendas pertenecientes a la compania (Nivel 3).
 * Utiliza conmutacion dinamica multi-tenant y transacciones independientes por tienda.
 */
@Service
public class PropagateCompanyProductToOutletsUseCaseImpl implements PropagateCompanyProductToOutletsUseCase {

    private static final Logger log = LoggerFactory.getLogger(PropagateCompanyProductToOutletsUseCaseImpl.class);

    private final JpaProductCompanyRepository productCompanyRepository;
    private final JpaGlobalProductRepository globalProductRepository;
    private final JpaProductOutletRepository productOutletRepository;
    private final OutletRepositoryPort outletRepositoryPort;
    private final BarcodeRepositoryPort barcodeRepositoryPort;
    private final MasterTreeProvider masterTreeProvider;
    private final TransactionTemplate transactionTemplate;

    public PropagateCompanyProductToOutletsUseCaseImpl(
            JpaProductCompanyRepository productCompanyRepository,
            JpaGlobalProductRepository globalProductRepository,
            JpaProductOutletRepository productOutletRepository,
            OutletRepositoryPort outletRepositoryPort,
            BarcodeRepositoryPort barcodeRepositoryPort,
            MasterTreeProvider masterTreeProvider,
            PlatformTransactionManager transactionManager
    ) {
        this.productCompanyRepository = productCompanyRepository;
        this.globalProductRepository = globalProductRepository;
        this.productOutletRepository = productOutletRepository;
        this.outletRepositoryPort = outletRepositoryPort;
        this.barcodeRepositoryPort = barcodeRepositoryPort;
        this.masterTreeProvider = masterTreeProvider;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    public int execute(Long productCompanyId) {
        ProductCompanyEntity productCompany = productCompanyRepository.findById(productCompanyId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto corporativo con ID " + productCompanyId + " no encontrado"));

        Product product = globalProductRepository.findById(productCompany.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto global con ID " + productCompany.getProductId() + " no encontrado"));

        List<OutletDomain> outlets = outletRepositoryPort.findByCompanyId(productCompany.getCompanyId());
        if (outlets == null || outlets.isEmpty()) {
            log.info("No se encontraron tiendas activas para la compania con ID {}", productCompany.getCompanyId());
            return 0;
        }

        MasterTree tree = masterTreeProvider.getTree();
        MasterRoot actStatus = tree.getByCode("ACT");
        Long activeStatusId = actStatus != null ? actStatus.getId() : 1L;

        int propagatedCount = 0;

        for (OutletDomain outlet : outlets) {
            Long prevTenantId = TenantContext.getTenantId();
            Long prevOutletId = TenantContext.getTenantOutletId();

            try {
                if (outlet.getCompanyId() != null) {
                    TenantContext.setTenantId(outlet.getCompanyId());
                }
                TenantContext.setTenantOutletId(outlet.getId());

                transactionTemplate.execute(status -> {
                    Optional<ProductOutlet> existing = productOutletRepository
                            .findByProductCompanyIdAndOutletId(productCompany.getId(), outlet.getId());

                    ProductOutlet productOutlet;
                    if (existing.isPresent()) {
                        productOutlet = existing.get();
                        productOutlet.setLocalPrice(productCompany.getCustomPrice());
                        productOutlet.setStatusId(activeStatusId);
                        productOutlet = productOutletRepository.save(productOutlet);
                    } else {
                        List<String> imageUrls = Collections.emptyList();
                        if (productCompany.getCustomImageUrl() != null && !productCompany.getCustomImageUrl().isBlank()) {
                            imageUrls = List.of(productCompany.getCustomImageUrl());
                        } else if (product.getImageUrl() != null && !product.getImageUrl().isBlank()) {
                            imageUrls = List.of(product.getImageUrl());
                        }

                        productOutlet = ProductOutlet.builder()
                                .productCompanyId(productCompany.getId())
                                .localName(product.getName())
                                .localDescription(product.getDescription())
                                .stock(0)
                                .unitMeasureId(product.getUnitMeasureId() != null ? product.getUnitMeasureId() : 1L)
                                .localImageUrl(imageUrls)
                                .localPrice(productCompany.getCustomPrice())
                                .outletId(outlet.getId())
                                .statusId(activeStatusId)
                                .build();
                        productOutlet = productOutletRepository.save(productOutlet);
                    }

                    // Propagacion o sincronizacion de codigo de barras
                    if (product.getBarcode() != null && !product.getBarcode().isBlank()) {
                        Optional<BarcodeDomain> existingBarcode = barcodeRepositoryPort.findByCode(product.getBarcode());
                        if (existingBarcode.isEmpty()) {
                            BarcodeDomain newBarcode = BarcodeDomain.create(
                                    product.getBarcode(),
                                    productOutlet.getId(),
                                    "Codigo homologado Avalon"
                            );
                            barcodeRepositoryPort.save(newBarcode);
                        }
                    }
                    return null;
                });

                propagatedCount++;
            } catch (Exception ex) {
                log.error("Error al propagar producto {} hacia tienda {}: {}", product.getName(), outlet.getId(), ex.getMessage(), ex);
            } finally {
                TenantContext.setTenantId(prevTenantId);
                TenantContext.setTenantOutletId(prevOutletId);
            }
        }

        log.info("Producto {} (Nivel 2 ID {}) propagado exitosamente a {} tiendas de la compania {}",
                product.getName(), productCompany.getId(), propagatedCount, productCompany.getCompanyId());

        return propagatedCount;
    }
}

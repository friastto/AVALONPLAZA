package org.frias.avalon.domain.product.application.usecase.linkbarcode;

import org.frias.avalon.core.exeptions.DomainValidationException;
import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.core.tenant.TenantContext;
import org.frias.avalon.domain.outlet.infraestructure.entities.Outlet;
import org.frias.avalon.domain.outlet.infraestructure.repository.JpaOutletRepository;
import org.frias.avalon.domain.product.application.dto.request.LinkBarcodeRequest;
import org.frias.avalon.domain.product.application.port.ProductOutletRepositoryPort;
import org.frias.avalon.domain.product.domain.BarcodeDomain;
import org.frias.avalon.domain.product.domain.ProductDomain;
import org.frias.avalon.domain.product.domain.repository.BarcodeRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Optional;

/**
 * Caso de uso para vincular un codigo de barras adicional a un producto existente.
 * Utiliza TransactionTemplate con PROPAGATION_REQUIRES_NEW y conmutacion dinamica multi-tenant.
 */
@Service
public class LinkBarcodeToProductUseCaseImpl implements LinkBarcodeToProductUseCase {

    private final ProductOutletRepositoryPort productOutletRepositoryPort;
    private final BarcodeRepositoryPort barcodeRepositoryPort;
    private final JpaOutletRepository jpaOutletRepository;
    private final TransactionTemplate transactionTemplate;

    public LinkBarcodeToProductUseCaseImpl(
            ProductOutletRepositoryPort productOutletRepositoryPort,
            BarcodeRepositoryPort barcodeRepositoryPort,
            JpaOutletRepository jpaOutletRepository,
            PlatformTransactionManager transactionManager
    ) {
        this.productOutletRepositoryPort = productOutletRepositoryPort;
        this.barcodeRepositoryPort = barcodeRepositoryPort;
        this.jpaOutletRepository = jpaOutletRepository;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    public void execute(LinkBarcodeRequest request) {
        BarcodeDomain newBarcode;
        try {
            newBarcode = BarcodeDomain.create(
                    request.barcode(),
                    request.productId(),
                    request.description()
            );
        } catch (DomainValidationException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }

        Long previousOutletId = TenantContext.getTenantOutletId();
        Long previousTenantId = TenantContext.getTenantId();

        try {
            // Caso 1: Verificar en el contexto actual si ya tiene tienda
            if (previousOutletId != null) {
                Boolean linked = doLinkInCurrentTenant(request.productId(), newBarcode);
                if (Boolean.TRUE.equals(linked)) {
                    return;
                }
            }

            // Caso 2: Recorrer tiendas registradas para ubicar el producto
            List<Outlet> outlets = jpaOutletRepository.findAll();
            for (Outlet o : outlets) {
                if (o.getCompanyId() != null) {
                    TenantContext.setTenantId(o.getCompanyId());
                }
                TenantContext.setTenantOutletId(o.getId());

                Boolean linked = doLinkInCurrentTenant(request.productId(), newBarcode);
                if (Boolean.TRUE.equals(linked)) {
                    return;
                }
            }

            throw new ResourceNotFoundException("Producto no encontrado con ID: " + request.productId());
        } finally {
            TenantContext.setTenantId(previousTenantId);
            TenantContext.setTenantOutletId(previousOutletId);
        }
    }

    private Boolean doLinkInCurrentTenant(Long productId, BarcodeDomain newBarcode) {
        try {
            return transactionTemplate.execute(status -> {
                Optional<ProductDomain> productOpt = productOutletRepositoryPort.findById(productId);
                if (productOpt.isEmpty()) {
                    return false;
                }
                barcodeRepositoryPort.save(newBarcode);
                return true;
            });
        } catch (Exception e) {
            return false;
        }
    }
}

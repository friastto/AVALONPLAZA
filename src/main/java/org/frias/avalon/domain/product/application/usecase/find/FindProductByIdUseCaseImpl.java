package org.frias.avalon.domain.product.application.usecase.find;

import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.core.tenant.TenantContext;
import org.frias.avalon.domain.outlet.domain.model.OutletDomain;
import org.frias.avalon.domain.outlet.domain.port.OutletRepositoryPort;
import org.frias.avalon.domain.outlet.infraestructure.entities.Outlet;
import org.frias.avalon.domain.outlet.infraestructure.repository.JpaOutletRepository;
import org.frias.avalon.domain.product.application.dto.response.ProductResponse;
import org.frias.avalon.domain.product.application.port.ProductOutletRepositoryPort;
import org.frias.avalon.domain.product.domain.ProductDomain;
import org.frias.avalon.domain.product.domain.BarcodeDomain;
import org.frias.avalon.domain.product.domain.repository.BarcodeRepositoryPort;
import org.frias.avalon.domain.product.infraestructure.mapper.ProductOutletMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Optional;

@Service
public class FindProductByIdUseCaseImpl implements FindProductByIdUseCase {

    private final ProductOutletRepositoryPort productOutletRepositoryPort;
    private final BarcodeRepositoryPort barcodeRepositoryPort;
    private final ProductOutletMapper productOutletMapper;
    private final OutletRepositoryPort outletPort;
    private final JpaOutletRepository jpaOutletRepository;
    private final TransactionTemplate transactionTemplate;

    public FindProductByIdUseCaseImpl(
            ProductOutletRepositoryPort productOutletRepositoryPort,
            BarcodeRepositoryPort barcodeRepositoryPort,
            ProductOutletMapper productOutletMapper,
            OutletRepositoryPort outletPort,
            JpaOutletRepository jpaOutletRepository,
            PlatformTransactionManager transactionManager) {
        this.productOutletRepositoryPort = productOutletRepositoryPort;
        this.barcodeRepositoryPort = barcodeRepositoryPort;
        this.productOutletMapper = productOutletMapper;
        this.outletPort = outletPort;
        this.jpaOutletRepository = jpaOutletRepository;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    public ProductResponse execute(Long productId) {
        return execute(productId, null);
    }

    @Override
    public ProductResponse execute(Long productId, Long outletId) {
        Long previousOutletId = TenantContext.getTenantOutletId();
        Long previousTenantId = TenantContext.getTenantId();

        try {
            // Caso 1: Se proporciono outletId explicitamente
            if (outletId != null) {
                OutletDomain outlet = outletPort.findById(outletId).orElse(null);
                if (outlet != null) {
                    if (outlet.getCompanyId() != null) {
                        TenantContext.setTenantId(outlet.getCompanyId());
                    }
                    TenantContext.setTenantOutletId(outlet.getId());
                }
                ProductResponse response = findInCurrentTenant(productId);
                if (response != null) {
                    return response;
                }
                throw new ResourceNotFoundException("El producto con ID " + productId + " no fue encontrado en la tienda con ID " + outletId);
            }

            // Caso 2: Verificar en el contexto actual si ya tiene tenant configurado
            if (previousOutletId != null) {
                ProductResponse response = findInCurrentTenant(productId);
                if (response != null) {
                    return response;
                }
            }

            // Caso 3: Buscar a traves de las tiendas existentes en el sistema
            List<Outlet> outlets = jpaOutletRepository.findAll();
            for (Outlet o : outlets) {
                if (o.getCompanyId() != null) {
                    TenantContext.setTenantId(o.getCompanyId());
                }
                TenantContext.setTenantOutletId(o.getId());
                ProductResponse response = findInCurrentTenant(productId);
                if (response != null) {
                    return response;
                }
            }

            throw new ResourceNotFoundException("El producto con ID " + productId + " no fue encontrado en la base de datos.");
        } finally {
            TenantContext.setTenantId(previousTenantId);
            TenantContext.setTenantOutletId(previousOutletId);
        }
    }

    private ProductResponse findInCurrentTenant(Long productId) {
        try {
            return transactionTemplate.execute(status -> {
                Optional<ProductDomain> productOpt = productOutletRepositoryPort.findById(productId);
                if (productOpt.isEmpty()) {
                    return null;
                }
                ProductDomain productDomain = productOpt.get();

                String barcode = barcodeRepositoryPort.findByProductOutlet(productId).stream()
                        .map(BarcodeDomain::getBarcode)
                        .findFirst()
                        .orElse(null);

                return productOutletMapper.toResponse(productDomain, barcode);
            });
        } catch (Exception e) {
            return null;
        }
    }
}

package org.frias.avalon.domain.product.application.usecase.find;

import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.core.tenant.TenantContext;
import org.frias.avalon.domain.outlet.infraestructure.entities.Outlet;
import org.frias.avalon.domain.outlet.infraestructure.repository.JpaOutletRepository;
import org.frias.avalon.domain.product.application.dto.response.ProductResponse;
import org.frias.avalon.domain.product.application.port.ProductOutletRepositoryPort;
import org.frias.avalon.domain.product.domain.BarcodeDomain;
import org.frias.avalon.domain.product.domain.ProductDomain;
import org.frias.avalon.domain.product.domain.repository.BarcodeRepositoryPort;
import org.frias.avalon.domain.product.infraestructure.mapper.ProductOutletMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Optional;

/**
 * Caso de uso para buscar productos por codigo de barras.
 * Implementa aislamiento transaccional con TransactionTemplate (PROPAGATION_REQUIRES_NEW)
 * y conmutacion/fallback dinamico multi-tenant a traves de tiendas.
 */
@Service
public class FindProductByBarcodeUseCaseImpl implements FindProductByBarcodeUseCase {

    private final BarcodeRepositoryPort barcodeRepositoryPort;
    private final ProductOutletRepositoryPort productOutletRepositoryPort;
    private final ProductOutletMapper productOutletMapper;
    private final JpaOutletRepository jpaOutletRepository;
    private final TransactionTemplate transactionTemplate;

    public FindProductByBarcodeUseCaseImpl(
            BarcodeRepositoryPort barcodeRepositoryPort,
            ProductOutletRepositoryPort productOutletRepositoryPort,
            ProductOutletMapper productOutletMapper,
            JpaOutletRepository jpaOutletRepository,
            PlatformTransactionManager transactionManager
    ) {
        this.barcodeRepositoryPort = barcodeRepositoryPort;
        this.productOutletRepositoryPort = productOutletRepositoryPort;
        this.productOutletMapper = productOutletMapper;
        this.jpaOutletRepository = jpaOutletRepository;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    public ProductResponse execute(String barcode) {
        Long previousOutletId = TenantContext.getTenantOutletId();
        Long previousTenantId = TenantContext.getTenantId();

        try {
            // Caso 1: Verificar en el contexto actual si ya tiene tienda configurada
            if (previousOutletId != null) {
                ProductResponse response = findInCurrentTenant(barcode);
                if (response != null) {
                    return response;
                }
            }

            // Caso 2: Recorrido de fallback a traves de las tiendas existentes en el sistema
            List<Outlet> outlets = jpaOutletRepository.findAll();
            for (Outlet o : outlets) {
                if (o.getCompanyId() != null) {
                    TenantContext.setTenantId(o.getCompanyId());
                }
                TenantContext.setTenantOutletId(o.getId());
                ProductResponse response = findInCurrentTenant(barcode);
                if (response != null) {
                    return response;
                }
            }

            throw new ResourceNotFoundException("No se encontro ningun producto asociado al codigo de barras: " + barcode);
        } finally {
            TenantContext.setTenantId(previousTenantId);
            TenantContext.setTenantOutletId(previousOutletId);
        }
    }

    private ProductResponse findInCurrentTenant(String barcode) {
        try {
            return transactionTemplate.execute(status -> {
                Optional<BarcodeDomain> barcodeOpt = barcodeRepositoryPort.findByCode(barcode);
                if (barcodeOpt.isEmpty()) {
                    return null;
                }
                BarcodeDomain barcodeDomain = barcodeOpt.get();
                Optional<ProductDomain> productOpt = productOutletRepositoryPort.findById(barcodeDomain.getProductOutletId());
                if (productOpt.isEmpty()) {
                    return null;
                }
                return productOutletMapper.toResponse(productOpt.get(), barcodeDomain.getBarcode());
            });
        } catch (Exception e) {
            return null;
        }
    }
}

package org.frias.avalon.domain.product.application.usecase.find;

import org.frias.avalon.core.exeptions.BusinessException;
import org.frias.avalon.core.permissions.CurrentUserProviderPort;
import org.frias.avalon.core.tenant.TenantContext;
import org.frias.avalon.domain.outlet.domain.model.OutletDomain;
import org.frias.avalon.domain.outlet.domain.port.OutletRepositoryPort;
import org.frias.avalon.domain.product.application.dto.response.ProductResponse;
import org.frias.avalon.domain.product.application.port.ProductOutletRepositoryPort;
import org.frias.avalon.domain.product.infraestructure.mapper.ProductOutletMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Caso de uso para obtener el catálogo de productos de una tienda.
 * Garantiza que los empleados de una tienda estén encapsulados en su propia tienda.
 */
@Service
public class FindProductCatalogByOutletUseCaseImpl implements FindProductCatalogByOutletUseCase {

    private final ProductOutletRepositoryPort productOutletRepositoryPort;
    private final ProductOutletMapper productOutletMapper;
    private final CurrentUserProviderPort currentUserProvider;
    private final OutletRepositoryPort outletPort;
    private final TransactionTemplate transactionTemplate;

    public FindProductCatalogByOutletUseCaseImpl(
            ProductOutletRepositoryPort productOutletRepositoryPort,
            ProductOutletMapper productOutletMapper,
            CurrentUserProviderPort currentUserProvider,
            OutletRepositoryPort outletPort,
            TransactionTemplate transactionTemplate) {
        this.productOutletRepositoryPort = productOutletRepositoryPort;
        this.productOutletMapper = productOutletMapper;
        this.currentUserProvider = currentUserProvider;
        this.outletPort = outletPort;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public Page<ProductResponse> execute(Long outletId, String name, Long categoryId, Pageable pageable) {
        // --- Validar Encapsulacion de Tienda (Tenant Isolation) ---
        boolean isConsumer = currentUserProvider.hasRole("ROLE_CLIENT") || currentUserProvider.hasRole("ROLE_CONSUMER");
        boolean isSystemAdmin = currentUserProvider.hasRole("ROLE_ADMIN") || currentUserProvider.hasRole("ROLE_ADMINTI");

        if (!isSystemAdmin && !isConsumer) {
            Long tenantOutletId = currentUserProvider.getCurrentOutletId();
            if (tenantOutletId != null && !tenantOutletId.equals(outletId)) {
                throw new BusinessException("Acceso denegado: No tienes permisos para ver el catalogo de otra tienda.");
            }
        }

        // Aislamiento Multi-Tenant: cambiar contexto al esquema de la tienda solicitada
        if (outletId != null) {
            OutletDomain outlet = outletPort.findById(outletId).orElse(null);
            if (outlet != null) {
                if (outlet.getCompanyId() != null) {
                    TenantContext.setTenantId(outlet.getCompanyId());
                }
                TenantContext.setTenantOutletId(outlet.getId());
            }
        }

        try {
            Page<ProductResponse> result = transactionTemplate.execute(status ->
                    productOutletRepositoryPort.findAll(name, outletId, categoryId, pageable)
                            .map(productOutletMapper::toResponse)
            );
            return result != null ? result : Page.empty(pageable);
        } catch (Exception e) {
            return Page.empty(pageable);
        } finally {
            TenantContext.clear();
        }
    }
}

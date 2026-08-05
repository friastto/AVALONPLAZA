package org.frias.avalon.domain.product.application.usecase.find;

import lombok.RequiredArgsConstructor;
import org.frias.avalon.core.exeptions.BusinessException;
import org.frias.avalon.core.permissions.CurrentUserProviderPort;
import org.frias.avalon.domain.product.application.dto.response.ProductResponse;
import org.frias.avalon.domain.product.application.port.ProductOutletRepositoryPort;
import org.frias.avalon.domain.product.infraestructure.mapper.ProductOutletMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso para obtener el catálogo de productos de una tienda.
 * Garantiza que los empleados de una tienda estén encapsulados en su propia tienda.
 */
@Service
@RequiredArgsConstructor
public class FindProductCatalogByOutletUseCaseImpl implements FindProductCatalogByOutletUseCase {

    private final ProductOutletRepositoryPort productOutletRepositoryPort;
    private final ProductOutletMapper productOutletMapper;
    private final CurrentUserProviderPort currentUserProvider;

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> execute(Long outletId, String name, Pageable pageable) {
        // --- Validar Encapsulacion de Tienda (Tenant Isolation) ---
        boolean isConsumer = currentUserProvider.hasRole("ROLE_CLIENT") || currentUserProvider.hasRole("ROLE_CONSUMER");
        boolean isSystemAdmin = currentUserProvider.hasRole("ROLE_ADMIN") || currentUserProvider.hasRole("ROLE_ADMINTI");

        if (!isSystemAdmin && !isConsumer) {
            Long tenantOutletId = currentUserProvider.getCurrentOutletId();
            if (tenantOutletId != null && !tenantOutletId.equals(outletId)) {
                throw new BusinessException("Acceso denegado: No tienes permisos para ver el catalogo de otra tienda.");
            }
        }

        return productOutletRepositoryPort.findAll(name, outletId, pageable)
                .map(productOutletMapper::toResponse);
    }
}

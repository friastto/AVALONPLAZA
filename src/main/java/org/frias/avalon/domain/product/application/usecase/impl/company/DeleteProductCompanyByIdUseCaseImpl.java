package org.frias.avalon.domain.product.application.usecase.impl.company;

import org.frias.avalon.core.exeptions.AccessDeniedException;
import org.frias.avalon.domain.company.facade.TenantSecurity;
import org.frias.avalon.domain.product.application.mapper.ProductoMapperService;
import org.frias.avalon.domain.product.application.services.interfaces.ProductoService;
import org.frias.avalon.domain.product.application.usecase.inter.company.DeleteProductCompanyByIdUseCase;
import org.springframework.stereotype.Service;

@Service
public class DeleteProductCompanyByIdUseCaseImpl implements DeleteProductCompanyByIdUseCase {
    private final TenantSecurity tenantSecurity;
    private final ProductoMapperService productoMapperService;

    private final ProductoService productoService;


    public DeleteProductCompanyByIdUseCaseImpl(TenantSecurity tenantSecurity, ProductoMapperService productoMapperService, ProductoService productoService) {
        this.tenantSecurity = tenantSecurity;
        this.productoMapperService = productoMapperService;
        this.productoService = productoService;
    }


    @Override
    public void execute(Long id) {

        if (!tenantSecurity.isMasterStaff()) {
            throw new AccessDeniedException("Solo Avalon-Admin puede borrar productos de Avalon");
        }

        productoService.deleteById(id);
    }
}

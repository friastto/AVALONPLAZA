package org.frias.avalon.domain.product.application.usecase.impl.company;

import org.frias.avalon.core.exeptions.AccessDeniedException;
import org.frias.avalon.domain.company.facade.TenantSecurity;
import org.frias.avalon.domain.product.application.dto.company.ProductResponseDto;
import org.frias.avalon.domain.product.application.mapper.ProductoCompanyMapperService;
import org.frias.avalon.domain.product.application.mapper.ProductoMapperService;
import org.frias.avalon.domain.product.application.services.interfaces.ProductoService;
import org.frias.avalon.domain.product.application.usecase.company.SearchProductCompanyByIdUseCase;
import org.springframework.stereotype.Service;

@Service
public class SearchProductCompanyByIdUseCaseImpl implements SearchProductCompanyByIdUseCase {
    private final TenantSecurity tenantSecurity;
    private final ProductoCompanyMapperService productoCompanyMapperService;

    private final ProductoService productoService;

    public SearchProductCompanyByIdUseCaseImpl(TenantSecurity tenantSecurity, ProductoCompanyMapperService productoCompanyMapperService, ProductoService productoService) {
        this.tenantSecurity = tenantSecurity;
        this.productoCompanyMapperService = productoCompanyMapperService;

        this.productoService = productoService;
    }


    @Override
    public ProductResponseDto execute(Long id) {

        if (!tenantSecurity.isMasterStaff()) {
            throw new AccessDeniedException("Solo SaaS Admin puede crear productos");
        }

        return null; //productoCompanyMapperService.toDto(productoService.searchById(id));
    }
}

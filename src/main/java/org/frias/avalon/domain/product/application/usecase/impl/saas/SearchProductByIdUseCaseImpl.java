package org.frias.avalon.domain.product.application.usecase.impl.saas;

import org.frias.avalon.core.exeptions.AccessDeniedException;
import org.frias.avalon.domain.company.facade.TenantSecurity;
import org.frias.avalon.domain.product.application.dto.company.ProductResponseDto;
import org.frias.avalon.domain.product.application.dto.saas.ProductAvalonResponseDto;
import org.frias.avalon.domain.product.application.mapper.ProductoMapperService;
import org.frias.avalon.domain.product.application.services.interfaces.ProductoService;
import org.frias.avalon.domain.product.application.usecase.saas.SearchProductByIdUseCase;
import org.springframework.stereotype.Service;


@Service
public class SearchProductByIdUseCaseImpl  implements SearchProductByIdUseCase {
   private final TenantSecurity tenantSecurity;
   private final ProductoMapperService productoMapperService;

    private final ProductoService productoService;

    public SearchProductByIdUseCaseImpl(TenantSecurity tenantSecurity, ProductoMapperService productoMapperService, ProductoService productoService) {
        this.tenantSecurity = tenantSecurity;
        this.productoMapperService = productoMapperService;
        this.productoService = productoService;
    }


    @Override
    public ProductAvalonResponseDto execute(Long id) {

        if(!tenantSecurity.isMasterStaff()){
            throw new AccessDeniedException("Solo SaaS Admin puede crear productos");
        }

        return productoMapperService.toDto(productoService.searchById(id));
    }
}

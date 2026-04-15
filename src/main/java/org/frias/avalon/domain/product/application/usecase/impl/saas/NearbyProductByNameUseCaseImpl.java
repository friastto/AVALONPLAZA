package org.frias.avalon.domain.product.application.usecase.impl.saas;

import org.frias.avalon.core.exeptions.AccessDeniedException;
import org.frias.avalon.domain.company.facade.TenantSecurity;
import org.frias.avalon.domain.product.application.dto.saas.ProductAvalonResponseDto;
import org.frias.avalon.domain.product.application.mapper.ProductoMapperService;
import org.frias.avalon.domain.product.application.services.interfaces.ProductoService;
import org.frias.avalon.domain.product.application.usecase.inter.saas.NearbyProductByNameUseCase;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class NearbyProductByNameUseCaseImpl implements NearbyProductByNameUseCase {
   private final TenantSecurity tenantSecurity;
   private final ProductoMapperService productoMapperService;

    private final ProductoService productoService;

    public NearbyProductByNameUseCaseImpl(TenantSecurity tenantSecurity, ProductoMapperService productoMapperService, ProductoService productoService) {
        this.tenantSecurity = tenantSecurity;
        this.productoMapperService = productoMapperService;
        this.productoService = productoService;
    }


    @Override
    public List<ProductAvalonResponseDto> execute(String name) {

        if(!tenantSecurity.isMasterStaff()){
            throw new AccessDeniedException("Solo SaaS Admin puede buscar productos por coincidencias en el name");
        }

        return productoService.nearbyNameProduct(name).stream()
                .map(productoMapperService::toDto)
                .toList();
    }
}

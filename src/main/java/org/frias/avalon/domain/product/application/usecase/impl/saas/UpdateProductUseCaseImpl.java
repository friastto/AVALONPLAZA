package org.frias.avalon.domain.product.application.usecase.impl.saas;

import org.frias.avalon.core.exeptions.AccessDeniedException;
import org.frias.avalon.domain.company.facade.TenantSecurity;
import org.frias.avalon.domain.product.application.dto.saas.ProductAvalonRequestDataDto;
import org.frias.avalon.domain.product.application.dto.saas.ProductAvalonResponseDto;
import org.frias.avalon.domain.product.application.mapper.ProductoMapperService;
import org.frias.avalon.domain.product.application.services.interfaces.ProductoService;
import org.frias.avalon.domain.product.application.usecase.inter.saas.UpdateProductUseCase;
import org.springframework.stereotype.Service;

@Service
public class UpdateProductUseCaseImpl extends TenantSecurity implements UpdateProductUseCase {
   private final ProductoService ps;
    private final ProductoService productoService;
    private final ProductoMapperService productoMapperService;

    public UpdateProductUseCaseImpl(ProductoService ps, ProductoService productoService, ProductoMapperService productoMapperService) {
        this.ps = ps;
        this.productoService = productoService;
        this.productoMapperService = productoMapperService;
    }


    @Override
    public ProductAvalonResponseDto execute(Long idproduct, ProductAvalonRequestDataDto dto) {

        if(!isMasterStaff()){
            throw new AccessDeniedException("Solo SaaS Admin puede crear productos");
        }

        return productoMapperService.toDto(
                    productoService.update(
                        idproduct,
                        dto.name(),
                        dto.description(),
                        dto.categoryId(),
                        dto.unitMeasureId()
                    )
                );
    }
}

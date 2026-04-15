package org.frias.avalon.domain.product.application.usecase.impl.saas;

import org.frias.avalon.core.exeptions.AccessDeniedException;
import org.frias.avalon.domain.company.facade.TenantSecurity;
import org.frias.avalon.domain.product.application.dto.company.ProductRequestCreate;
import org.frias.avalon.domain.product.application.dto.saas.ProductAvalonResponseDto;
import org.frias.avalon.domain.product.application.mapper.ProductoMapperService;
import org.frias.avalon.domain.product.application.services.interfaces.ProductoService;
import org.frias.avalon.domain.product.application.usecase.saas.DisableProductByIdUseCase;
import org.frias.avalon.domain.product.application.usecase.saas.UpdateProductUseCase;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DisableProductUseCaseImpl extends TenantSecurity implements DisableProductByIdUseCase {
   private final ProductoService ps;
    private final ProductoService productoService;
    private final ProductoMapperService productoMapperService;

    public DisableProductUseCaseImpl(ProductoService ps, ProductoService productoService, ProductoMapperService productoMapperService) {
        this.ps = ps;
        this.productoService = productoService;
        this.productoMapperService = productoMapperService;
    }


    @Override
    public ProductAvalonResponseDto execute(Long idProduct) {

        if(!isMasterStaff()){
            throw new AccessDeniedException("Solo SaaS Admin puede desabilitar productos");
        }

        return productoMapperService.toDto(productoService.disableProductById(idProduct));
    }
}

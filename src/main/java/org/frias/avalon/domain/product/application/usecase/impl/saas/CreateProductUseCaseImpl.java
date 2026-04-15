package org.frias.avalon.domain.product.application.usecase.impl.saas;

import org.frias.avalon.core.exeptions.AccessDeniedException;
import org.frias.avalon.domain.company.facade.TenantSecurity;
import org.frias.avalon.domain.product.application.dto.saas.ProductAvalonRequestDataDto;
import org.frias.avalon.domain.product.application.dto.saas.ProductAvalonResponseDto;
import org.frias.avalon.domain.product.application.mapper.ProductoMapperService;
import org.frias.avalon.domain.product.application.services.interfaces.ProductAvalonService;
import org.frias.avalon.domain.product.application.usecase.inter.saas.CreateProductUseCase;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CreateProductUseCaseImpl extends TenantSecurity implements CreateProductUseCase {

   private final ProductAvalonService productoService;
    private final ProductoMapperService productoMapperService;

    public CreateProductUseCaseImpl(ProductAvalonService productoService, ProductoMapperService productoMapperService) {
        this.productoService = productoService;
        this.productoMapperService = productoMapperService;
    }

    @Override
    public ProductAvalonResponseDto execute(ProductAvalonRequestDataDto dto, MultipartFile image) {


        if (!isMasterStaff())
            throw new AccessDeniedException("Solo Avalon-Admin puede crear productos en Avalon");

        return productoMapperService.toDto(productoService.createProduct(
                dto.name(),
                dto.description(),
                dto.categoryId(),
                dto.unitMeasureId(),
                image
        ));
    }
}

package org.frias.avalon.domain.product.application.usecase.inter.saas;

import org.frias.avalon.domain.product.application.dto.company.ProductRequestCreate;

import org.frias.avalon.domain.product.application.dto.saas.ProductAvalonResponseDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


public interface GetAllProductsUseCase {
    List<ProductAvalonResponseDto> execute();
}
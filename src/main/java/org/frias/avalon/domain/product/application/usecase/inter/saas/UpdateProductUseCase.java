package org.frias.avalon.domain.product.application.usecase.inter.saas;

import org.frias.avalon.domain.product.application.dto.company.ProductRequestCreate;
import org.frias.avalon.domain.product.application.dto.company.ProductResponseDto;

import org.frias.avalon.domain.product.application.dto.saas.ProductAvalonRequestDataDto;
import org.frias.avalon.domain.product.application.dto.saas.ProductAvalonResponseDto;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;



public interface UpdateProductUseCase {
    ProductAvalonResponseDto execute(Long idProduct, ProductAvalonRequestDataDto dto);
}
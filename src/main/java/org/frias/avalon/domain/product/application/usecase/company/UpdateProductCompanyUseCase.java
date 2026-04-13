package org.frias.avalon.domain.product.application.usecase.company;

import org.frias.avalon.domain.product.application.dto.company.ProductRequestCreate;
import org.frias.avalon.domain.product.application.dto.company.ProductResponseDto;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


public interface UpdateProductCompanyUseCase {
    ProductResponseDto execute(ProductRequestCreate dto, MultipartFile file);
}
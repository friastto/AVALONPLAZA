package org.frias.avalon.domain.product.application.usecase.saas;

import org.frias.avalon.domain.product.application.dto.company.ProductResponseDto;

import org.frias.avalon.domain.product.application.dto.saas.ProductAvalonResponseDto;
import org.springframework.stereotype.Service;



public interface SearchProductByIdUseCase {
    ProductAvalonResponseDto execute(Long id);
}
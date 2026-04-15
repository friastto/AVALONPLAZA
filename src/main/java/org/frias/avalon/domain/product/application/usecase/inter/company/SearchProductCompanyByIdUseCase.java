package org.frias.avalon.domain.product.application.usecase.inter.company;

import org.frias.avalon.domain.product.application.dto.company.ProductResponseDto;

import org.springframework.stereotype.Service;


public interface SearchProductCompanyByIdUseCase {
    ProductResponseDto execute(Long id);
}
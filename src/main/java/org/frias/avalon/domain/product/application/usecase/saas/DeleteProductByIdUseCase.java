package org.frias.avalon.domain.product.application.usecase.saas;

import org.frias.avalon.domain.product.application.dto.company.ProductResponseDto;

import org.springframework.stereotype.Service;



public interface DeleteProductByIdUseCase {
    void execute(Long id);
}
package org.frias.avalon.domain.product.application.usecase.inter.saas;


import org.frias.avalon.domain.product.application.dto.saas.ProductAvalonResponseDto;

public interface DisableProductByIdUseCase {
    ProductAvalonResponseDto execute(Long id);
}
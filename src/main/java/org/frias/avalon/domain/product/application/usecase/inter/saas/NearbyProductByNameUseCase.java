package org.frias.avalon.domain.product.application.usecase.inter.saas;

import org.frias.avalon.domain.product.application.dto.saas.ProductAvalonResponseDto;

import java.util.List;


public interface NearbyProductByNameUseCase {
    List<ProductAvalonResponseDto> execute(String name);
}
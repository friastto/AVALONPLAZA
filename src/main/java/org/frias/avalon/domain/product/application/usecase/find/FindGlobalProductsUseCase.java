package org.frias.avalon.domain.product.application.usecase.find;

import org.frias.avalon.domain.product.application.dto.response.GlobalProductResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FindGlobalProductsUseCase {
    Page<GlobalProductResponseDto> execute(String query, Pageable pageable);
}

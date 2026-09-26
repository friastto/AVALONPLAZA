package org.frias.avalon.domain.product.application.usecase.adopt;

import org.frias.avalon.domain.product.application.dto.request.AdoptGlobalProductRequestDto;
import org.frias.avalon.domain.product.application.dto.response.ProductSuggestionResponseDto;

public interface AdoptGlobalProductUseCase {
    ProductSuggestionResponseDto execute(AdoptGlobalProductRequestDto request);
}

package org.frias.avalon.domain.product.application.usecase.find;

import org.frias.avalon.domain.product.application.dto.request.NearbyStoresByProductRequestDto;
import org.frias.avalon.domain.product.application.dto.response.NearbyStoreProductResponseDto;

import java.util.List;

public interface FindNearbyStoresByProductUseCase {
    List<NearbyStoreProductResponseDto> execute(NearbyStoresByProductRequestDto request);
}

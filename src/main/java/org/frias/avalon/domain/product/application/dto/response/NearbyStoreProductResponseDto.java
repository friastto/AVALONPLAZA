package org.frias.avalon.domain.product.application.dto.response;

import java.util.List;

public record NearbyStoreProductResponseDto(
        Long id,
        String name,
        Double latitude,
        Double longitude,
        List<ProductStockDto> matchingProducts
) {
}

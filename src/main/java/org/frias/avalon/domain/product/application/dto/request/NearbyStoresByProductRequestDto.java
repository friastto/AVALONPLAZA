package org.frias.avalon.domain.product.application.dto.request;

import org.frias.avalon.domain.outlet.application.dto.LocationDto;

public record NearbyStoresByProductRequestDto(
        LocationDto location,
        int radius,
        String query
) {
}

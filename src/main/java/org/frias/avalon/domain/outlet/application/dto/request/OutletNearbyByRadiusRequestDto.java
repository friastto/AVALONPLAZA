package org.frias.avalon.domain.outlet.application.dto.request;

import org.frias.avalon.domain.outlet.application.dto.LocationDto;

public record OutletNearbyByRadiusRequestDto (
        LocationDto location,
        int radius
){
}

package org.frias.avalon.domain.outlet.application.dto.request;

import org.frias.avalon.domain.outlet.application.dto.LocationDto;

public record OutletCreateRequestDto(
        String name,
        String address,
        String phone,
        LocationDto location

) {
}

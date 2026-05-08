package org.frias.avalon.domain.outlet.application.dto.response;

import org.frias.avalon.domain.masterdata.application.dto.response.StatusResponseDto;
import org.frias.avalon.domain.outlet.application.dto.LocationDto;
import org.frias.avalon.domain.outlet.domain.model.LocationDomain;

public record OutletResponseDto(
        Long id,
        String code,
        String name,
        String address,
        String phone,
        LocationDto location,
        StatusResponseDto statusResponseDto





) {
}

package org.frias.avalon.domain.outlet.application.dto.response;

import org.frias.avalon.domain.masterdata.application.dto.response.StatusResponseDto;
import org.frias.avalon.domain.outlet.domain.model.LocationDomain;

public record OutletResponseDto(
        Long id,
        String name,
        String address,
        String phone,
        LocationDomain location,
        StatusResponseDto statusResponseDto





) {
}

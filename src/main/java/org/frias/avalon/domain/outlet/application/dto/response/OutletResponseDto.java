package org.frias.avalon.domain.outlet.application.dto.response;

import org.frias.avalon.domain.masterdata.application.dto.response.StatusResponseDto;
import org.frias.avalon.domain.outlet.application.dto.LocationDto;

import java.math.BigDecimal;

public record OutletResponseDto(
        Long id,
        String code,
        String name,
        String address,
        String phone,
        String nit,
        LocationDto location,
        StatusResponseDto statusResponseDto,
        Long companyId,
        Boolean deliveryEnabled,
        BigDecimal deliveryFee
) {
}
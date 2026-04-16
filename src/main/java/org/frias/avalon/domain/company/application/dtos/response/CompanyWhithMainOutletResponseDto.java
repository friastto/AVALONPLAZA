package org.frias.avalon.domain.company.application.dtos.response;

import org.frias.avalon.domain.outlet.dtos.response.OutletDto;

import java.util.List;

public record CompanyWhithMainOutletResponseDto(
        Long id,
        String nit,
        String name,
        String email,
        String status,
        OutletDto outlet
) {
}

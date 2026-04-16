package org.frias.avalon.domain.company.application.dtos;


import org.frias.avalon.domain.outlet.dtos.response.OutletDto;

import java.util.List;

public record CompanyResponseDto(
        Long id,

        String nit,

        String name,

        String email,

        List<OutletDto> outlets

) {
}

package org.frias.avalon.domain.company.A;


import org.frias.avalon.temp.empresasucursal.sucursal.dtos.OutletResponseDto;

import java.util.List;

public record CompanyResponseDto(
    Long id,

    String nit,

    String name,

    String email,

    List<OutletResponseDto> outlets

){}

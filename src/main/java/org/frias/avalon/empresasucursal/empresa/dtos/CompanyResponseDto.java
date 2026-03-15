package org.frias.avalon.empresasucursal.empresa.dtos;


import org.frias.avalon.empresasucursal.sucursal.dtos.OutletResponseDto;

import java.util.List;

public record CompanyResponseDto(
    Long id,

    String nit,

    String name,

    String email,

    List<OutletResponseDto> outlets

){}

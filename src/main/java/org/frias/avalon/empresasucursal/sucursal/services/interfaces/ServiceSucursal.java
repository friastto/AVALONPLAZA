package org.frias.avalon.empresasucursal.sucursal.services.interfaces;


import org.frias.avalon.empresasucursal.sucursal.dtos.OutletRequestNewDto;
import org.frias.avalon.empresasucursal.sucursal.dtos.OutletResponseDto;
import org.frias.avalon.empresasucursal.sucursal.dtos.SucursalDto;

import java.util.List;

public interface ServiceSucursal  {
    OutletResponseDto save(OutletRequestNewDto dto);

    List<OutletResponseDto> getAll();
}

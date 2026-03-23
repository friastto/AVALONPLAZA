package org.frias.avalon.temp.empresasucursal.sucursal.services.interfaces;


import org.frias.avalon.temp.empresasucursal.sucursal.dtos.OutletRequestNewDto;
import org.frias.avalon.temp.empresasucursal.sucursal.dtos.OutletResponseDto;
import org.frias.avalon.temp.empresasucursal.sucursal.dtos.OutletsRequestMap;

import java.util.List;

public interface ServiceSucursal  {
    OutletResponseDto save(OutletRequestNewDto dto);

    List<OutletResponseDto> getAll();

    List<OutletResponseDto> searchNearbyStores(OutletsRequestMap outletsRequestMap);
}

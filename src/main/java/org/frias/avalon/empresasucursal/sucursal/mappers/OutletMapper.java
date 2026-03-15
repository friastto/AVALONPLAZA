package org.frias.avalon.empresasucursal.sucursal.mappers;

import org.frias.avalon.empresasucursal.sucursal.dtos.OutletResponseDto;
import org.frias.avalon.empresasucursal.sucursal.entities.Outlet;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OutletMapper {

  public  OutletResponseDto toDto(Outlet outlet) {
        return new OutletResponseDto(
                outlet.getId(),
                outlet.getName(),
                outlet.getAddress(),
                outlet.getPhone(),
                outlet.getLatitude(),
                outlet.getLongitude()
        );
    }

    public List<OutletResponseDto> listEntityToListDto(List<Outlet> outlets) {

      List<OutletResponseDto> outletResponseDtos = new ArrayList<>();

      for (Outlet outlet : outlets) {
          outletResponseDtos.add(toDto(outlet));
      }

      return outletResponseDtos;
    }
}

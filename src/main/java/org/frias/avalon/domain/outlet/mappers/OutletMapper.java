package org.frias.avalon.domain.outlet.mappers;

import org.frias.avalon.domain.outlet.dtos.response.OutletDto;
import org.frias.avalon.domain.outlet.entities.Outlet;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OutletMapper {

  public OutletDto toDto(Outlet outlet) {
        return new OutletDto(
                outlet.getId(),
                outlet.getName(),
                outlet.getAddress(),
                outlet.getPhone(),
                outlet.getLatitude(),
                outlet.getLongitude()
        );
    }

    public List<OutletDto> listEntityToListDto(List<Outlet> outlets) {

      List<OutletDto> outletDtos = new ArrayList<>();

      for (Outlet outlet : outlets) {
          outletDtos.add(toDto(outlet));
      }

      return outletDtos;
    }
}

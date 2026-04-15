package org.frias.avalon.domain.outlet.services.interfaces;


import org.frias.avalon.domain.outlet.dtos.request.OutletMap;
import org.frias.avalon.domain.outlet.dtos.request.OutletNewDto;
import org.frias.avalon.domain.outlet.dtos.response.OutletDto;
import org.frias.avalon.domain.outlet.entities.Outlet;

import java.util.List;

public interface OutletService {


    Outlet create(OutletNewDto dto);

    List<OutletDto> getAll();

    Outlet searchById(Long id);

    List<OutletDto> searchNearbyStores(OutletMap outletMap);

    Boolean existsByIdAndCompanyId(Long idOutlets, Long idCompany);


    Outlet create(
            Long idCompany,
            String name,
            String address,
            String phone,
            Double latitude,
            Double longitude,
            boolean isMain
    );
}

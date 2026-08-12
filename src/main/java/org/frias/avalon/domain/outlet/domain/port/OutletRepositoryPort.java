package org.frias.avalon.domain.outlet.domain.port;

import org.frias.avalon.domain.outlet.domain.model.LocationDomain;
import org.frias.avalon.domain.outlet.domain.model.OutletDomain;
import org.frias.avalon.domain.outlet.domain.model.OutletLocationInfo;

import org.frias.avalon.domain.outlet.application.dto.request.OutletSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface OutletRepositoryPort {

    OutletDomain save(OutletDomain domain);

    Optional<OutletDomain> findById(Long id);

    Optional<OutletDomain> findByNit(String nit);

    List<OutletDomain> findByCompanyId(Long companyId);

    Page<OutletDomain> findAll(OutletSearchCriteria criteria, Pageable pageable);

    List<OutletDomain> nearbyByName(String domain);

    OutletDomain update(OutletDomain domain);

    OutletDomain delete(OutletDomain domain);

    List<OutletDomain> findNearbyByRadius(LocationDomain location, int radius);

    List<OutletLocationInfo> findNearbyByRadiusLight(Double latitude, Double longitude, int radius);

}
package org.frias.avalon.domain.outlet.domain.port;

import org.frias.avalon.domain.outlet.domain.model.LocationDomain;
import org.frias.avalon.domain.outlet.domain.model.OutletDomain;

import java.util.List;
import java.util.Optional;

public interface OutletRepositoryPort {

    OutletDomain save(OutletDomain domain);
    Optional<OutletDomain> findById(Long id);
    OutletDomain findAll();
    List<OutletDomain> nearbyByName(String domain);
    OutletDomain update(OutletDomain domain);
    OutletDomain delete(OutletDomain domain);
    List<OutletDomain> findNearbyByRadius(LocationDomain location, int radius);


}

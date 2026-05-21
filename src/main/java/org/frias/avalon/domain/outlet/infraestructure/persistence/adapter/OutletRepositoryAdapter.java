package org.frias.avalon.domain.outlet.infraestructure.persistence.adapter;

import org.frias.avalon.domain.outlet.domain.model.LocationDomain;
import org.frias.avalon.domain.outlet.domain.model.OutletDomain;
import org.frias.avalon.domain.outlet.domain.port.OutletRepositoryPort;
import org.frias.avalon.domain.outlet.infraestructure.entities.Outlet;
import org.frias.avalon.domain.outlet.infraestructure.mapper.LocationMapper;
import org.frias.avalon.domain.outlet.infraestructure.mapper.OutletMapper;
import org.frias.avalon.domain.outlet.infraestructure.repository.JpaOutletRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class OutletRepositoryAdapter implements OutletRepositoryPort {

    private final JpaOutletRepository jpa;
    private final OutletMapper outletMapper;
    private final LocationMapper locationMapper;

    public OutletRepositoryAdapter(JpaOutletRepository jpa, OutletMapper outletMapper, LocationMapper locationMapper) {
        this.jpa = jpa;
        this.outletMapper = outletMapper;
        this.locationMapper = locationMapper;
    }

    @Override
    public OutletDomain save(OutletDomain domain) {

        Outlet o = outletMapper.toEntity(domain);

        return outletMapper.toDomain(jpa.save(o));
    }

    @Override
    public Optional<OutletDomain> findById(Long id) {


        return jpa.findById(id).map(outletMapper::toDomain);
    }

    @Override
    public OutletDomain findAll() {
        return null;
    }

    @Override
    public List<OutletDomain> nearbyByName(String domain) {
        return List.of();
    }

    @Override
    public OutletDomain update(OutletDomain domain) {
        return null;
    }

    @Override
    public OutletDomain delete(OutletDomain domain) {
        return null;
    }

    @Override
    public List<OutletDomain> findNearbyByRadius(LocationDomain location, int radius) {

        List<Outlet> outletsList = jpa.findNearByOrderByDistance(location.longitude(), location.latitude(), radius);

        return outletsList.stream().map(outletMapper::toDomain).toList();
    }
}

package org.frias.avalon.domain.outlet.infraestructure.persistence.adapter;

import org.frias.avalon.domain.outlet.domain.model.LocationDomain;
import org.frias.avalon.domain.outlet.domain.model.OutletDomain;
import org.frias.avalon.domain.outlet.domain.model.OutletLocationInfo;
import org.frias.avalon.domain.outlet.domain.port.OutletRepositoryPort;
import org.frias.avalon.domain.outlet.infraestructure.entities.Outlet;
import org.frias.avalon.domain.outlet.infraestructure.mapper.LocationMapper;
import org.frias.avalon.domain.outlet.infraestructure.mapper.OutletMapper;
import org.frias.avalon.domain.outlet.infraestructure.repository.JpaOutletRepository;
import org.frias.avalon.domain.outlet.infraestructure.repository.OutletLightProjection;
import org.frias.avalon.domain.outlet.application.dto.request.OutletSearchCriteria;
import org.frias.avalon.domain.outlet.infraestructure.specification.OutletSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    public Optional<OutletDomain> findByNit(String nit) {
        return jpa.findByNit(nit).map(outletMapper::toDomain);
    }

    @Override
    public List<OutletDomain> findByCompanyId(Long companyId) {
        return jpa.findByCompanyId(companyId).stream()
                .map(outletMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Page<OutletDomain> findAll(OutletSearchCriteria criteria, Pageable pageable) {
        Specification<Outlet> spec = Specification.allOf(
                OutletSpecification.hasName(criteria.name()),
                OutletSpecification.hasNit(criteria.nit()),
                OutletSpecification.hasCode(criteria.code()),
                OutletSpecification.hasAddress(criteria.address()),
                OutletSpecification.hasStatusId(criteria.statusId())
        );

        return jpa.findAll(spec, pageable)
                .map(outletMapper::toDomain);
    }

    @Override
    public List<OutletDomain> nearbyByName(String domain) {
        return List.of();
    }

    @Override
    public OutletDomain update(OutletDomain domain) {
        Outlet o = outletMapper.toEntity(domain);
        return outletMapper.toDomain(jpa.save(o));
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

    @Override
    public List<OutletLocationInfo> findNearbyByRadiusLight(Double latitude, Double longitude, int radius) {
        List<OutletLightProjection> projections = jpa.findNearbyByRadiusLight(latitude, longitude, radius);
        return projections.stream()
                .map(p -> new OutletLocationInfo(p.getId(), p.getName(), p.getLatitude(), p.getLongitude()))
                .collect(Collectors.toList());
    }
}
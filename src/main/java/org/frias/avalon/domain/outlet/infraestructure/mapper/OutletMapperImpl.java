package org.frias.avalon.domain.outlet.infraestructure.mapper;

import org.frias.avalon.domain.outlet.application.dto.response.OutletResponseDto;
import org.frias.avalon.domain.outlet.domain.model.LocationDomain;
import org.frias.avalon.domain.outlet.domain.model.OutletDomain;
import org.frias.avalon.domain.outlet.infraestructure.entities.Outlet;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Component;

@Component
public class OutletMapperImpl implements OutletMapper {

    private final LocationMapper locationMapper;


    public OutletMapperImpl(LocationMapper locationMapper) {
        this.locationMapper = locationMapper;
    }


    @Override
    public OutletDomain toDomain(Outlet o) {

        LocationDomain location = locationMapper.toLocation(o.getLocation());

        return OutletDomain.fromPersistence(
                o.getId(),
                o.getCode(),
                o.getName(),
                o.getAddress(),
                o.getPhone(),
                o.getStatusId(),
                location
        );
    }

    @Override
    public Outlet toEntity(OutletDomain od) {

        Point point = locationMapper.toPoint(od.getLocation());


        return Outlet.builder()
                .name(od.getName())
                .address(od.getAddress())
                .phone(od.getPhone())
                .statusId(od.getStatusId())
                .location(point)
                .build();
    }

    @Override
    public OutletResponseDto toResponse(OutletDomain od) {
        return null;
    }
}

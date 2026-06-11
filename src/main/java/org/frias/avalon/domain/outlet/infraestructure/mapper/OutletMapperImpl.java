package org.frias.avalon.domain.outlet.infraestructure.mapper;

import org.frias.avalon.domain.masterdata.application.dto.response.StatusResponseDto;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.outlet.application.dto.LocationDto;
import org.frias.avalon.domain.outlet.application.dto.response.OutletResponseDto;
import org.frias.avalon.domain.outlet.domain.model.LocationDomain;
import org.frias.avalon.domain.outlet.domain.model.OutletDomain;
import org.frias.avalon.domain.outlet.infraestructure.entities.Outlet;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Component;

@Component
public class OutletMapperImpl implements OutletMapper {

    private final LocationMapper locationMapper;
    private final MasterTreeProvider masterTreeProvider;

    public OutletMapperImpl(LocationMapper locationMapper, MasterTreeProvider masterTreeProvider) {
        this.locationMapper = locationMapper;
        this.masterTreeProvider = masterTreeProvider;
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
                o.getNit(),
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
                .nit(od.getNit())
                .statusId(od.getStatusId())
                .location(point)
                .build();
    }

    @Override
    public OutletResponseDto toResponse(OutletDomain od) {
        MasterRoot status = masterTreeProvider.getTree().getById(od.getStatusId());
        LocationDto locationDto = locationMapper.domainToDto(od.getLocation());
        StatusResponseDto statusResponseDto = new StatusResponseDto(status.getId(), status.getShortName(), status.getFullName());

        return new OutletResponseDto(
                od.getId(),
                od.getCode(),
                od.getName(),
                od.getAddress(),
                od.getPhone(),
                od.getNit(),
                locationDto,
                statusResponseDto
        );
    }
}
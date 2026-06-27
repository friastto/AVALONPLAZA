package org.frias.avalon.domain.outlet.infraestructure.mapper;

import org.frias.avalon.domain.outlet.application.dto.LocationDto;
import org.frias.avalon.domain.outlet.domain.model.LocationDomain;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;

@Service
public class LocationMapperImpl implements LocationMapper {
    private final GeometryFactory geometryFactory;

    public LocationMapperImpl(GeometryFactory geometryFactory) {
        this.geometryFactory = geometryFactory;
    }

    @Override
    public LocationDomain toLocation(Point point) {

        if (point == null) return null;

        return new LocationDomain(
                point.getY(), // lat
                point.getX()  // lon
        );

    }

    @Override
    public Point toPoint(LocationDomain location) {
        if (location == null) return null;

        return geometryFactory.createPoint(
                new Coordinate(
                        location.longitude(), // OJO: lon primero
                        location.latitude()
                )
        );

    }

    @Override
    public LocationDomain dtoToDomain(LocationDto dto) {

        return new LocationDomain(dto.lat(), dto.lon());
    }

    @Override
    public LocationDto domainToDto(LocationDomain domain) {
        return new LocationDto(domain.latitude(), domain.longitude());
    }

}

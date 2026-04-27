package org.frias.avalon.domain.outlet.infraestructure.mapper;

import org.frias.avalon.domain.outlet.domain.model.LocationDomain;

import org.locationtech.jts.geom.Point;

public interface LocationMapper {

    LocationDomain toLocation(Point point);
    Point toPoint (LocationDomain locattion);
}

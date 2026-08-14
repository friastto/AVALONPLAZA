package org.frias.avalon.domain.outlet.domain.service;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

/**
 * Pure Java domain utility for geographic coordinate processing.
 * Free of Spring Framework annotations.
 */
public class GeoUtil {

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    public Point createPoint(double lat, double lon) {
        return geometryFactory.createPoint(new Coordinate(lon, lat));
    }
}

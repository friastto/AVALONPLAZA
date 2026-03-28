package org.frias.avalon.domain.outlet.dtos.request;

public record OutletMap(
        String query,
        Double lat,
        Double lng,
        int radius
) {
}

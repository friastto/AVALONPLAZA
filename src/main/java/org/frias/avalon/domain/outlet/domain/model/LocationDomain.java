package org.frias.avalon.domain.outlet.domain.model;

public class LocationDomain {

    private final Double latitude;
    private final Double longitude;

    public LocationDomain(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Double latitude() {
        return latitude;
    }

    public Double longitude() {
        return longitude;
    }
}

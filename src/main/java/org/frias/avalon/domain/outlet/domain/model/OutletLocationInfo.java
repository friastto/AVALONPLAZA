package org.frias.avalon.domain.outlet.domain.model;

/**
 * A lightweight domain model representing the essential location information of an outlet.
 * This object is pure domain logic and has no framework dependencies.
 *
 * @param id        The unique identifier.
 * @param name      The name of the outlet.
 * @param latitude  The geographical latitude.
 * @param longitude The geographical longitude.
 */
public record OutletLocationInfo(
        Long id,
        String name,
        Double latitude,
        Double longitude
) {
}
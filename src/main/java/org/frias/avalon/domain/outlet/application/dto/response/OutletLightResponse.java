package org.frias.avalon.domain.outlet.application.dto.response;

/**
 * DTO for representing a lightweight version of an Outlet, suitable for map markers.
 *
 * @param id        The unique identifier of the outlet.
 * @param name      The name of the outlet.
 * @param latitude  The latitude of the outlet's location.
 * @param longitude The longitude of the outlet's location.
 */
public record OutletLightResponse(
        Long id,
        String name,
        Double latitude,
        Double longitude
) {
}
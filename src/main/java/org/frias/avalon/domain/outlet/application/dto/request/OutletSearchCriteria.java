package org.frias.avalon.domain.outlet.application.dto.request;

public record OutletSearchCriteria(
        String name,
        String nit,
        String code,
        String address,
        Long statusId
) {
}

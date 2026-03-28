package org.frias.avalon.domain.outlet.dtos.response;


public record OutletDto(
        Long id,

    String name,

    String address,

    String phone,

    Double latitude,

    Double longitude
    ){}

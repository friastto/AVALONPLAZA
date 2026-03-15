package org.frias.avalon.empresasucursal.sucursal.dtos;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OutletResponseDto(
        Long id,

    String name,

    String address,

    String phone,

    Double latitude,

    Double longitude
    ){}

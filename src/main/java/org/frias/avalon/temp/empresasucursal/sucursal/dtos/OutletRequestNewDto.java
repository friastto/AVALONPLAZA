package org.frias.avalon.temp.empresasucursal.sucursal.dtos;


import jakarta.validation.constraints.*;

public record OutletRequestNewDto(


        @NotBlank(message = "La sucursal no puede estar sin nombre")
    String name,

        @NotBlank(message = "ingrese la direccion de la sucursal")
    String address,

        @NotBlank(message = "ingrese un numero de telefono")
        @Pattern(
                regexp = "^(\\d{7}|\\d{10})$",
                message = "El teléfono debe tener 7 dígitos (fijo) o 10 dígitos (móvil)"
        )
    String phone,

        @NotNull(message = "Debe seleccionar la ubicación exacta en el mapa")
        @Min(value = -90, message = "Latitud inválida")
        @Max(value = 90, message = "Latitud inválida")
    Double latitude,

        @NotNull(message = "Debe seleccionar la ubicación exacta en el mapa")
        @Min(value = -180, message = "Longitud inválida")
        @Max(value = 180, message = "Longitud inválida")
    Double longitude
    ){}

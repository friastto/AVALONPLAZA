package org.frias.avalon.domain.outlet.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.frias.avalon.domain.outlet.application.dto.LocationDto;

public record OutletCreateRequestDto(
        @NotBlank(message = "El nombre es obligatorio")
        String name,
        @NotBlank(message = "La direccion es obligatoria")
        String address,
        @NotBlank(message = "El telefono es obligatorio")
        String phone,
        @NotBlank(message = "El NIT es obligatorio")
        String nit,
        @NotNull(message = "La ubicacion es obligatoria")
        LocationDto location,
        @NotNull(message = "El ID de la empresa es obligatorio")
        Long companyId
) {
}
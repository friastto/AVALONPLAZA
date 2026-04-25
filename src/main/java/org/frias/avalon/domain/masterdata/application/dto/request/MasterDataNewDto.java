package org.frias.avalon.domain.masterdata.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record MasterDataNewDto(

        @NotBlank(message = "el name largo no puede ser vacio")
        @Pattern(regexp = "^[^<>]*$", message = "No se permiten etiquetas HTML")
        String fullName,

        @NotBlank(message = "el name corto no puede ser vacio")
        @Pattern(regexp = "^[^<>]*$", message = "No se permiten etiquetas HTML")
        String shortName,

        String parentShortName,

        @NotBlank(message = "el name corto de status no puede ser vacio")
        String  statusId
){
}

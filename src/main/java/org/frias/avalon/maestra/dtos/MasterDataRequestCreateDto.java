package org.frias.avalon.maestra.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record MasterDataRequestCreateDto (
        @NotNull(message = "el nombre largo no puede ser vacio")
        @NotBlank(message = "el nombre largo no puede ser vacio")
        @NotEmpty(message = "el nombre largo no puede ser vacio")
        String fullName,

        @NotNull(message = "el nombre corto no puede ser vacio")
        @NotBlank(message = "el nombre corto no puede ser vacio")
        @NotEmpty(message = "el nombre corto no puede ser vacio")
        String shortName,

        @NotNull(message = "el nombre corto de raiz no puede ser vacio")
        @NotBlank(message = "el nombre corto de raiz puede ser vacio")
        @NotEmpty(message = "el nombre corto de raiz ser vacio")
        String parentShortName,

        @NotNull(message = "el nombre corto de status no puede ser vacio")
        @NotBlank(message = "el nombre corto de status no puede ser vacio")
        @NotEmpty(message = "el nombre corto de status no puede ser vacio")
        String  statusShortName
){
}

package org.frias.avalon.domain.masterdata.dtos.request;

import jakarta.validation.constraints.NotBlank;

public record MasterDataNewDto(

        @NotBlank(message = "el nombre largo no puede ser vacio")
        String fullName,

        @NotBlank(message = "el nombre corto no puede ser vacio")
        String shortName,

        String parentShortName,

        @NotBlank(message = "el nombre corto de status no puede ser vacio")
        String  statusId
){
}

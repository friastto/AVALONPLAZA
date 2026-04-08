package org.frias.avalon.domain.masterdata.dtos.request;

import jakarta.validation.constraints.NotBlank;

public record MasterDataNewDto(

        @NotBlank(message = "el name largo no puede ser vacio")
        String fullName,

        @NotBlank(message = "el name corto no puede ser vacio")
        String shortName,

        String parentShortName,

        @NotBlank(message = "el name corto de status no puede ser vacio")
        String  statusId
){
}

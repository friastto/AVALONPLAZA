package org.frias.avalon.domain.masterdata.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public record MasterDataUpdateStatusDto(
        @NotBlank(message = "Debe seleccionar la data a  actualizar")
        Long current,
        @NotBlank(message = "Debe seleccionar el estado nuevo")
        Long next
) {
}

package org.frias.avalon.domain.user.application.dtos.request;

import jakarta.validation.constraints.NotBlank;

public record ApplicantLookupRequestDto(
        @NotBlank(message = "El numero de identificacion es obligatorio")
        String identificationNumber
) {
}

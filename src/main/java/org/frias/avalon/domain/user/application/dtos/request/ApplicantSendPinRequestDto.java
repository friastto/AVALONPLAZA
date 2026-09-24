package org.frias.avalon.domain.user.application.dtos.request;

import jakarta.validation.constraints.NotBlank;

public record ApplicantSendPinRequestDto(
        @NotBlank(message = "El numero de identificacion es obligatorio")
        String identificationNumber
) {
}

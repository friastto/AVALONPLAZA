package org.frias.avalon.domain.user.application.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ApplicantVerifyPinRequestDto(
        @NotBlank(message = "El numero de identificacion es obligatorio")
        String identificationNumber,

        @NotBlank(message = "El PIN es obligatorio")
        @Size(min = 6, max = 6, message = "El PIN debe tener exactamente 6 digitos")
        String pin
) {
}

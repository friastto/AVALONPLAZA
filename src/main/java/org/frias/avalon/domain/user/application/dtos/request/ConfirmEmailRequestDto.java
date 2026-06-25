package org.frias.avalon.domain.user.application.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ConfirmEmailRequestDto(
        @NotBlank(message = "El correo no puede estar vacío")
        @Email(message = "Debe ser una dirección de correo válida")
        String email
) {
}
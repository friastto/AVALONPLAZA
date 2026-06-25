package org.frias.avalon.domain.user.application.dtos.request;

import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequestDto(
        @NotBlank(message = "El identificador (email, usuario o cédula) no puede estar vacío")
        String identifier
) {
}
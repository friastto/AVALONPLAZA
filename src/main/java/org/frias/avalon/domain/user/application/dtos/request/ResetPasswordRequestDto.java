package org.frias.avalon.domain.user.application.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequestDto(
        @NotBlank(message = "El token de verificación no puede estar vacío")
        String verificationToken,

        @NotBlank(message = "La nueva contraseña no puede estar vacía")
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        String newPassword
) {
}
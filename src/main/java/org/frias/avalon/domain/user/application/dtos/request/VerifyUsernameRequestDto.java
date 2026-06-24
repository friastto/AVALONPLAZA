package org.frias.avalon.domain.user.application.dtos.request;

import jakarta.validation.constraints.NotBlank;

public record VerifyUsernameRequestDto(
        @NotBlank(message = "El nombre de usuario no puede estar vacío")
        String userName
) {
}
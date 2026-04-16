package org.frias.avalon.domain.user.domain.dtos.request;

import jakarta.validation.constraints.NotBlank;

public record UserValidateCredentials(
        @NotBlank(message = "digite el name de usuario")
        String userName,

        @NotBlank(message = "digite la contraseña")
        String password) {}

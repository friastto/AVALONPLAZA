package org.frias.avalon.useravalon.dtos;

import jakarta.validation.constraints.NotBlank;

public record UserValidateCredentials(
        @NotBlank(message = "digite el nombre de usuario")
        String userName,

        @NotBlank(message = "digite la contraseña")
        String password) {}

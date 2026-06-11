package org.frias.avalon.domain.user.application.dtos.request;

import jakarta.validation.constraints.NotBlank;


public record AuthRequest(

        @NotBlank(message = "ingrese un usuario valido")
        String identifier,

        @NotBlank(message = "Ingrese una contraseña valida")
        String password


) {
}



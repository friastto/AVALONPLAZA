package org.frias.avalon.domain.user.application.dtos.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UserNewDto(
        @NotBlank(message = "No puede crear un usuario sin UserName")
        @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Username inválido")
        String userName,
        @NotBlank(message = "ingrese una contraseña")
        String password

) {
}

package org.frias.avalon.domain.usergeneral.auth.dtos.request;

import jakarta.validation.constraints.NotBlank;



public record AuthRequest (

    @NotBlank(message = "ingrese un usuario valido")
    String username,

    @NotBlank(message = "Ingrese una contraseña valida")
    String password
    ){
}



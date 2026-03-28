package org.frias.avalon.domain.usergeneral.auth.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token; // respuesta con el JWT
}

package org.frias.avalon.domain.user.domain.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token; // respuesta con el JWT
}

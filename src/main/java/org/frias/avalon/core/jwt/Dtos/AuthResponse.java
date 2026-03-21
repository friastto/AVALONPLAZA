package org.frias.avalon.core.jwt.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token; // respuesta con el JWT
}

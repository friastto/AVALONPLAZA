package org.frias.avalon.domain.user.application.authservices.interfaces;

import jakarta.validation.constraints.NotBlank;
import org.frias.avalon.domain.user.domain.dtos.request.AuthRequest;
import org.frias.avalon.domain.user.domain.dtos.response.AuthResponse;

import java.security.spec.InvalidKeySpecException;

public interface AuthService {
    AuthResponse login(AuthRequest request) throws InvalidKeySpecException;

    AuthResponse login(
            String username,
            String password
    );
}

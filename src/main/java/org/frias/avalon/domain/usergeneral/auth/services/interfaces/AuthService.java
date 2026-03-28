package org.frias.avalon.domain.usergeneral.auth.services.interfaces;

import org.frias.avalon.domain.usergeneral.auth.dtos.request.AuthRequest;
import org.frias.avalon.domain.usergeneral.auth.dtos.response.AuthResponse;

import java.security.spec.InvalidKeySpecException;

public interface AuthService {
    AuthResponse login(AuthRequest request) throws InvalidKeySpecException;
}

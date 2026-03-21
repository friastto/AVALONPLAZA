package org.frias.avalon.core.jwt.services.interfaces;

import org.frias.avalon.core.jwt.Dtos.AuthRequest;
import org.frias.avalon.core.jwt.Dtos.AuthResponse;

import java.security.spec.InvalidKeySpecException;

public interface AuthService {
    AuthResponse login(AuthRequest request) throws InvalidKeySpecException;
}

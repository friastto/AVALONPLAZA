package org.frias.avalon.jwt.services.interfaces;

import org.frias.avalon.jwt.Dtos.AuthRequest;
import org.frias.avalon.jwt.Dtos.AuthResponse;

import java.security.spec.InvalidKeySpecException;

public interface AuthService {
    AuthResponse login(AuthRequest request) throws InvalidKeySpecException;
}

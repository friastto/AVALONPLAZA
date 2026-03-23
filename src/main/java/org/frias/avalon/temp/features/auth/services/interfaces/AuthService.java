package org.frias.avalon.temp.features.auth.services.interfaces;

import org.frias.avalon.temp.features.auth.dtos.AuthRequest;
import org.frias.avalon.temp.features.auth.dtos.AuthResponse;

import java.security.spec.InvalidKeySpecException;

public interface AuthService {
    AuthResponse login(AuthRequest request) throws InvalidKeySpecException;
}

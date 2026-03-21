package org.frias.avalon.useravalon.controllers;

import org.frias.avalon.core.jwt.Dtos.AuthRequest;
import org.frias.avalon.core.jwt.Dtos.AuthResponse;
import org.frias.avalon.core.jwt.services.interfaces.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.spec.InvalidKeySpecException;

@RestController
@RequestMapping("/auth-avalon")
public class AuthAvalonController {

    private final AuthService authService;

    public AuthAvalonController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping
    public AuthResponse validateCredentials(@RequestBody AuthRequest credentials) throws InvalidKeySpecException {

        return authService.login(credentials);
    }

}





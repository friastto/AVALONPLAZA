package org.frias.avalon.domain.usergeneral.auth.controllers;

import org.frias.avalon.domain.usergeneral.auth.dtos.request.AuthRequest;
import org.frias.avalon.domain.usergeneral.auth.dtos.response.AuthResponse;
import org.frias.avalon.domain.usergeneral.auth.services.interfaces.AuthService;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest credentials) throws InvalidKeySpecException {

        return ResponseEntity.ok(authService.login(credentials));
    }

}





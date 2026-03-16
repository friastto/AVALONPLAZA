package org.frias.avalon.useravalon.controllers;

import jakarta.validation.Valid;
import org.frias.avalon.empresasucursal.empresa.dtos.CompanyRequestNewDto;
import org.frias.avalon.empresasucursal.empresa.dtos.CompanyResponseDto;
import org.frias.avalon.empresasucursal.empresa.services.interfaces.CompanyService;
import org.frias.avalon.jwt.Dtos.AuthRequest;
import org.frias.avalon.jwt.Dtos.AuthResponse;
import org.frias.avalon.jwt.services.interfaces.AuthService;
import org.frias.avalon.useravalon.dtos.UserLinkPersonRequestDto;
import org.frias.avalon.useravalon.dtos.UserRequestNewDto;
import org.frias.avalon.useravalon.services.interfaces.UsuarioService;
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

    @PostMapping
    public AuthResponse validateCredentials(@RequestBody AuthRequest credentials) throws InvalidKeySpecException {

        return authService.login(credentials);
    }

}





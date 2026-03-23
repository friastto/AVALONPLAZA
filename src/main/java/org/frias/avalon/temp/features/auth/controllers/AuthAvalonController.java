package org.frias.avalon.temp.features.auth.controllers;

import org.frias.avalon.temp.features.auth.dtos.AuthRequest;
import org.frias.avalon.temp.features.auth.dtos.AuthResponse;
import org.frias.avalon.temp.features.auth.services.interfaces.AuthService;
import org.frias.avalon.domain.usergeneral.useravalon.dtos.UserRequestNewDto;
import org.frias.avalon.domain.usergeneral.useravalon.services.interfaces.UsersService;
import org.springframework.http.HttpStatus;
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
    private final UsersService usersService;

    public AuthAvalonController(AuthService authService, UsersService usersService) {
        this.authService = authService;
        this.usersService = usersService;
    }

    @PostMapping
    public ResponseEntity<AuthResponse> validateCredentials(@RequestBody AuthRequest credentials) throws InvalidKeySpecException {

        return ResponseEntity.ok(authService.login(credentials));
    }


    @PostMapping("/create")
    public ResponseEntity<Boolean> createPersonAndUser(@RequestBody UserRequestNewDto dto) {

        usersService.saveUserAndPerson(dto);

        return new ResponseEntity<>(true, HttpStatus.CREATED);
    }


}





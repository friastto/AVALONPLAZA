package org.frias.avalon.useravalon.controllers;

import org.frias.avalon.jwt.Dtos.AuthRequest;
import org.frias.avalon.jwt.Dtos.AuthResponse;
import org.frias.avalon.jwt.services.interfaces.AuthService;
import org.frias.avalon.useravalon.dtos.UserLinkPersonRequestDto;
import org.frias.avalon.useravalon.dtos.UserRequestNewDto;
import org.frias.avalon.useravalon.entities.UserAvalon;
import org.frias.avalon.useravalon.services.interfaces.UsuarioService;
import org.frias.avalon.useravalon.services.interfaces.UsuarioServiceValidate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.spec.InvalidKeySpecException;

@RestController
@RequestMapping("/userAvalon")
public class UserController {

    private final UsuarioService usuarioService;
    private final UsuarioServiceValidate usuarioServiceValidate;
    private final AuthService authService;


    public UserController(UsuarioService usuarioService, UsuarioServiceValidate usuarioServiceValidate, AuthService authService) {
        this.usuarioService = usuarioService;
        this.usuarioServiceValidate = usuarioServiceValidate;
        this.authService = authService;
    }


    @GetMapping("/searcActive")
    public UserAvalon searcActive(@RequestParam String numberId) {
        return usuarioService.getUserEmployeeStatus(numberId);
    }

    @PostMapping("/add")
    public ResponseEntity<Boolean> addNewUser(@RequestBody UserLinkPersonRequestDto userNew) throws InvalidKeySpecException {

        usuarioService.saveUserAndCreateLinkPerson(userNew);

        return ResponseEntity.ok(true);
    }
    @PostMapping("/save")
    public ResponseEntity<Boolean> savePersonAndUser(@RequestBody UserRequestNewDto userNew)  {

        usuarioService.saveUserAndPerson(userNew);

        return ResponseEntity.ok(true);
    }

    @PostMapping("/validateCredentials")
    public AuthResponse validateCredentials(@RequestBody AuthRequest credentials) throws InvalidKeySpecException {

       return authService.login(credentials);

    }

}

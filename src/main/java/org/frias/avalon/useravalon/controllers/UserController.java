package org.frias.avalon.useravalon.controllers;

import org.frias.avalon.useravalon.dtos.UserLinkPersonRequestDto;
import org.frias.avalon.useravalon.dtos.UserRequestNewDto;
import org.frias.avalon.useravalon.entities.UserAvalon;
import org.frias.avalon.useravalon.services.interfaces.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.spec.InvalidKeySpecException;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UsuarioService usuarioService;

    public UserController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }


    // Este método reemplaza al de los dos controladores anteriores
    @PostMapping("/link-user-to-person")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ADMINTI','GERGEN')")
    public ResponseEntity<Boolean> addNewUserlinkToPerson(@RequestBody UserLinkPersonRequestDto dto) throws InvalidKeySpecException {
        usuarioService.saveUserAndCreateLinkPerson(dto);
        return ResponseEntity.ok(true);
    }

    // Este también se unifica
    @PostMapping("/create")

    public ResponseEntity<Boolean> saveFull(@RequestBody UserRequestNewDto dto) {
        usuarioService.saveUserAndPerson(dto);
        return ResponseEntity.ok(true);
    }

}

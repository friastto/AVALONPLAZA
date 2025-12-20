package org.frias.avalon.user.controllers;

import org.frias.avalon.user.dtos.UserResponseDto;
import org.frias.avalon.user.entities.User;
import org.frias.avalon.user.services.interfaces.UsuarioService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UsuarioService usuarioService;


    public UserController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }


    @GetMapping("/searcActive")
    public User searcActive(@RequestParam String numberId) {
        return usuarioService.getUserEmployeeStatus(numberId);
    }

}

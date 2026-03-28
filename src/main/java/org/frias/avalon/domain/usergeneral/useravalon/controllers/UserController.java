package org.frias.avalon.domain.usergeneral.useravalon.controllers;

import org.frias.avalon.domain.usergeneral.useravalon.dtos.request.UserNewDto;
import org.frias.avalon.domain.usergeneral.useravalon.dtos.request.UserNewLinkPersonDto;
import org.frias.avalon.domain.usergeneral.useravalon.entities.UserAvalon;
import org.frias.avalon.domain.usergeneral.useravalon.services.interfaces.UsersService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.security.spec.InvalidKeySpecException;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UsersService usersService;

    public UserController(UsersService usersService) {
        this.usersService = usersService;
    }



    @PostMapping("/link-user-to-person")
    //@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ADMINTI','ROLE_GERGEN')")
    public ResponseEntity<Boolean> addNewUserlinkToPerson(@RequestBody UserNewLinkPersonDto dto) {
        try {

            usersService.createUserAndCreateLinkPerson(dto);

            return new ResponseEntity<>(true, HttpStatus.CREATED);

        } catch (InvalidKeySpecException e) {
            // Devuelve un error 400 o 500 según corresponda, sin ensuciar la firma
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error de encriptación", e);
        }
    }

    @PostMapping("/create")
    public ResponseEntity<UserAvalon> createPersonAndUser(@RequestBody UserNewDto dto) {
        UserAvalon ua
                = usersService.createUserAndPerson(dto);


        return new ResponseEntity<>(ua , HttpStatus.CREATED);
    }

}

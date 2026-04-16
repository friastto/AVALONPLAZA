package org.frias.avalon.domain.user.presentation.controller.company;

import org.frias.avalon.domain.user.domain.entities.UserAvalon;
import org.frias.avalon.domain.user.domain.dtos.request.UserNewDto;
import org.frias.avalon.domain.user.domain.dtos.request.UserNewLinkPersonDto;
import org.frias.avalon.domain.user.domain.services.interfaces.UsersService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.security.spec.InvalidKeySpecException;

@RestController
@RequestMapping("/avalon/admin/company/user")
public class UserCompanyController {

    private final UsersService usersService;

    public UserCompanyController(UsersService usersService) {
        this.usersService = usersService;
    }



    @PostMapping("/asign/person")
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

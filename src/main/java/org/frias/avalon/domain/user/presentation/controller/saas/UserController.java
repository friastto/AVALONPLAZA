package org.frias.avalon.domain.user.presentation.controller.saas;

import org.frias.avalon.core.exeptions.ApiResponse;
import org.frias.avalon.domain.user.application.usecase.inter.saas.CreateUserCompanyUseCase;
import org.frias.avalon.domain.user.domain.dtos.request.UserNewDto;
import org.frias.avalon.domain.user.domain.dtos.request.UserNewLinkPersonDto;
import org.frias.avalon.domain.user.domain.dtos.response.UserResponseDto;
import org.frias.avalon.domain.user.domain.entities.UserAvalon;
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
@RequestMapping("/avalon/saas/admin/user")
public class UserController {

    private final UsersService usersService;
private final CreateUserCompanyUseCase createUserCompanyUseCase;

    public UserController(UsersService usersService, CreateUserCompanyUseCase createUserCompanyUseCase) {
        this.usersService = usersService;
        this.createUserCompanyUseCase = createUserCompanyUseCase;
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
    public ResponseEntity<ApiResponse<UserResponseDto>> createPersonAndUser(@RequestBody UserNewDto dto) {
        UserResponseDto userCreated = createUserCompanyUseCase.execute(dto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(201,"se creo un usuario de tipo " + userCreated.getRole(),userCreated));
    }

}

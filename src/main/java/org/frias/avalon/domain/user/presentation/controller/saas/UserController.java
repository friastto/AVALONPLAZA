package org.frias.avalon.domain.user.presentation.controller.saas;

import org.frias.avalon.core.exeptions.ApiResponse;
import org.frias.avalon.domain.user.application.usecase.inter.saas.CreateUserUseCase;
import org.frias.avalon.domain.user.application.usecase.inter.saas.GetAllUserToCompanyUseCase;
import org.frias.avalon.domain.user.domain.dtos.request.UserNewDto;
import org.frias.avalon.domain.user.domain.dtos.request.UserNewLinkPersonDto;
import org.frias.avalon.domain.user.domain.dtos.response.UserAvalonDto;
import org.frias.avalon.domain.user.domain.dtos.response.UserResponseDto;
import org.frias.avalon.domain.user.domain.services.interfaces.UsersService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.spec.InvalidKeySpecException;
import java.util.List;

@RestController
@RequestMapping("/avalon/saas/admin/user")
public class UserController {

    private final UsersService usersService;
private final CreateUserUseCase createUserUseCase;
    private final GetAllUserToCompanyUseCase getAllUserToCompanyUseCase;

    public UserController(UsersService usersService, CreateUserUseCase createUserUseCase, GetAllUserToCompanyUseCase getAllUserToCompanyUseCase) {
        this.usersService = usersService;
        this.createUserUseCase = createUserUseCase;
        this.getAllUserToCompanyUseCase = getAllUserToCompanyUseCase;
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
        UserResponseDto userCreated = createUserUseCase.execute(dto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(201,"se creo un usuario de tipo " + userCreated.getRole(),userCreated));
    }
    @PostMapping("/linkperson")
    public ResponseEntity<ApiResponse<UserResponseDto>> LinkToPerson(@RequestBody UserNewLinkPersonDto dto) {
        UserResponseDto userCreated = createUserUseCase.execute(dto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(201,"se creo un usuario de tipo " + userCreated.getRole(),userCreated));
    }
    @GetMapping("/all/company/{id}")
    public ResponseEntity<ApiResponse<List<UserAvalonDto>>> getAllEmployeeCompany(@PathVariable Long id) {

        List<UserAvalonDto> userCompanyDto = getAllUserToCompanyUseCase.execute(id);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>(200,"extraccion de los usuarios de la empresa exitoso", userCompanyDto));
    }


}

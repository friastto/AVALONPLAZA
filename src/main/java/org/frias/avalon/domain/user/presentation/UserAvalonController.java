package org.frias.avalon.domain.user.presentation;

import org.frias.avalon.core.exeptions.ApiResponse;
import org.frias.avalon.domain.person.application.dto.request.CreatePersonRequest;
import org.frias.avalon.domain.user.application.dtos.request.AssignmentRoleRequestDto;
import org.frias.avalon.domain.user.application.dtos.request.ChangeUserAvalonStatusRequest;
import org.frias.avalon.domain.user.application.dtos.request.UserNewDto;
import org.frias.avalon.domain.user.application.dtos.response.AssignmentRoleResponse;
import org.frias.avalon.domain.user.application.dtos.response.UserAvalonDto;
import org.frias.avalon.domain.user.application.dtos.response.UserAvalonResponseDto;
import org.frias.avalon.domain.user.application.usecase.asignmentPerson.AssignPersonToUserUseCase;
import org.frias.avalon.domain.user.application.usecase.assingnrole.AssignmentRoleConsumerSelfUseCase;
import org.frias.avalon.domain.user.application.usecase.assingnrole.AssignmentRoleUseCase;
import org.frias.avalon.domain.user.application.usecase.changestatus.ChangeStatusUserAvalonUseCase;
import org.frias.avalon.domain.user.application.usecase.create.CreateUserAvalonUseCase;
import org.frias.avalon.domain.user.application.usecase.find.FindByUserNameUseCase;
import org.frias.avalon.domain.user.application.usecase.find.GetAllUserAvalonUseCase;
import org.frias.avalon.domain.user.application.usecase.login.LoginUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/avalon/user")
public class UserAvalonController {

    private final CreateUserAvalonUseCase createUser;
    private final ChangeStatusUserAvalonUseCase changeStatusUser;
    private final AssignmentRoleUseCase assignmentRole;
    private final GetAllUserAvalonUseCase getAllUserAvalonUseCase;
    private final FindByUserNameUseCase findByUserName;
    private final LoginUseCase loginUseCase;
    private final AssignmentRoleConsumerSelfUseCase consumerSelfUseCase;
    private final AssignPersonToUserUseCase assignmentPerson;

    public UserAvalonController(CreateUserAvalonUseCase createUser, ChangeStatusUserAvalonUseCase changeStatusUser, AssignmentRoleUseCase assignmentRole, GetAllUserAvalonUseCase getAllUserAvalonUseCase, FindByUserNameUseCase findByUserName, LoginUseCase loginUseCase, AssignmentRoleConsumerSelfUseCase consumerSelfUseCase, AssignPersonToUserUseCase assignmentPerson) {
        this.createUser = createUser;
        this.changeStatusUser = changeStatusUser;
        this.assignmentRole = assignmentRole;
        this.getAllUserAvalonUseCase = getAllUserAvalonUseCase;
        this.findByUserName = findByUserName;
        this.loginUseCase = loginUseCase;
        this.consumerSelfUseCase = consumerSelfUseCase;
        this.assignmentPerson = assignmentPerson;
    }


    @PostMapping("/create")
    public ResponseEntity<ApiResponse<UserAvalonResponseDto>> create(@RequestBody UserNewDto request) {

        UserAvalonResponseDto userCreated = createUser.execute(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                                201,
                                "se creo el usuario exitosamente",
                                userCreated
                        )
                );
    }

    @PatchMapping("/change/{UserId}/status")
    public ResponseEntity<ApiResponse<UserAvalonResponseDto>> changeStatus(@RequestBody ChangeUserAvalonStatusRequest request) {

        UserAvalonResponseDto userUpdated = changeStatusUser.execute(request);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>(
                                200,
                                "se cambio el estado del usuario a -> " + userUpdated.status(),
                                userUpdated
                        )
                );
    }

    @PatchMapping("/{userId}/assignment/role")
    public ResponseEntity<ApiResponse<AssignmentRoleResponse>> assignmentRole(@PathVariable Long userId, @RequestBody AssignmentRoleRequestDto request) {


        AssignmentRoleResponse userUpdated = assignmentRole.execute(new AssignmentRoleRequestDto(
                userId,
                request.roleId(),
                request.outletId()
        ));

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>(
                                200,
                                "se cambio el rol del usuario a -> " + userUpdated.role().fullName(),
                                userUpdated
                        )
                );
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<UserAvalonResponseDto>>> getAll() {

        List<UserAvalonResponseDto> userList = getAllUserAvalonUseCase.execute();

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>(
                                200,
                                "se obtuvieron * " + userList.size() + " * usuarios de avalon",
                                userList
                        )
                );
    }

    @GetMapping("/search/v1")
    public ResponseEntity<ApiResponse<UserAvalonResponseDto>> getByUserName(@RequestParam("userName") String userName) {

        UserAvalonResponseDto user = findByUserName.execute(userName);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>(
                                200,
                                "se obtuvo el usuario exitosamente",
                                user
                        )
                );
    }


    @PatchMapping("/assignment/role/consumer/self")
    public ResponseEntity<ApiResponse<AssignmentRoleResponse>> assignmentROleConsumerSelf() {

        AssignmentRoleResponse userUpdated = consumerSelfUseCase.execute();

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>(
                                200,
                                "se creo el rol -> " + userUpdated.role().fullName(),
                                userUpdated
                        )
                );
    }

    @PatchMapping("/{idUser}/assignment/person/")
    public ResponseEntity<ApiResponse<UserAvalonDto>> assignmentPerson(@PathVariable Long idUser, @RequestBody CreatePersonRequest newPersonData) {

        UserAvalonDto userUpdated = assignmentPerson.execute(idUser, newPersonData);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>(
                                200,
                                "se asigno la persona exitosamente",
                                userUpdated
                        )
                );
    }

}

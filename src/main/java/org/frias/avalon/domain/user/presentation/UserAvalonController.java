package org.frias.avalon.domain.user.presentation;

import org.frias.avalon.core.exeptions.ApiResponse;
import org.frias.avalon.domain.user.application.dtos.request.AssignmentRoleRequestDto;
import org.frias.avalon.domain.user.application.dtos.request.ChangeUserAvalonStatusRequest;
import org.frias.avalon.domain.user.application.dtos.request.UserNewDto;
import org.frias.avalon.domain.user.application.dtos.response.AssignmentRoleResponse;
import org.frias.avalon.domain.user.application.dtos.response.UserAvalonResponseDto;
import org.frias.avalon.domain.user.application.usecase.assingnrole.AssignmentRoleUseCase;
import org.frias.avalon.domain.user.application.usecase.changestatus.ChangeStatusUserAvalonUseCase;
import org.frias.avalon.domain.user.application.usecase.create.CreateUserAvalonUseCase;
import org.frias.avalon.domain.user.application.usecase.find.FindByUserNameUseCase;
import org.frias.avalon.domain.user.application.usecase.find.GetAllUserAvalonUseCase;
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

    public UserAvalonController(CreateUserAvalonUseCase createUser, ChangeStatusUserAvalonUseCase changeStatusUser, AssignmentRoleUseCase assignmentRole, GetAllUserAvalonUseCase getAllUserAvalonUseCase, FindByUserNameUseCase findByUserName) {
        this.createUser = createUser;
        this.changeStatusUser = changeStatusUser;
        this.assignmentRole = assignmentRole;
        this.getAllUserAvalonUseCase = getAllUserAvalonUseCase;
        this.findByUserName = findByUserName;
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
                                "se cambio el estado del usuario a -> "+userUpdated.status(),
                                userUpdated
                        )
                );
    }

    @PatchMapping("/assignment/role/{UserId}")
    public ResponseEntity<ApiResponse<AssignmentRoleResponse>> assignmentRole(@RequestBody AssignmentRoleRequestDto request) {

        AssignmentRoleResponse userUpdated = assignmentRole.execute(request);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>(
                                200,
                                "se cambio el estado del usuario a -> ",
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
                                "se obtuvieron * "+userList.size()+ " * usuarios de avalon",
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
}

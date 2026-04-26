package org.frias.avalon.domain.user.presentation;

import org.frias.avalon.core.exeptions.ApiResponse;
import org.frias.avalon.domain.user.application.dtos.request.ChangeUserAvalonStatusRequest;
import org.frias.avalon.domain.user.application.dtos.request.UserNewDto;
import org.frias.avalon.domain.user.application.dtos.response.UserAvalonResponseDto;
import org.frias.avalon.domain.user.application.usecase.changestatus.ChangeStatusUserAvalonUseCase;
import org.frias.avalon.domain.user.application.usecase.create.CreateUserAvalonUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/avalon/user")
public class UserAvalonController {

    private final CreateUserAvalonUseCase createUser;
    private final ChangeStatusUserAvalonUseCase changeStatusUser;

    public UserAvalonController(CreateUserAvalonUseCase createUser, ChangeStatusUserAvalonUseCase changeStatusUser) {
        this.createUser = createUser;
        this.changeStatusUser = changeStatusUser;
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
}

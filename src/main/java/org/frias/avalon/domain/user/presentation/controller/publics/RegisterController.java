package org.frias.avalon.domain.user.presentation.controller.publics;

import org.frias.avalon.core.exeptions.ApiResponse;
import org.frias.avalon.domain.user.application.usecase.inter.company.CreateUserUseCase;
import org.frias.avalon.domain.user.application.usecase.inter.publics.AuthloginUseCase;
import org.frias.avalon.domain.user.application.usecase.inter.publics.RegisterUseruseCase;
import org.frias.avalon.domain.user.domain.dtos.request.AuthRequest;
import org.frias.avalon.domain.user.domain.dtos.request.UserNewDto;
import org.frias.avalon.domain.user.domain.dtos.response.AuthResponse;
import org.frias.avalon.domain.user.domain.dtos.response.UserResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/avalon/public/register")
public class RegisterController {

private final RegisterUseruseCase registerUseruseCase;
private final AuthloginUseCase authloginUseCase;

    public RegisterController(RegisterUseruseCase registerUseruseCase, AuthloginUseCase authloginUseCase) {
        this.registerUseruseCase = registerUseruseCase;
        this.authloginUseCase = authloginUseCase;
    }


    @PostMapping("/create")
    public ResponseEntity<ApiResponse<AuthResponse>> createPersonAndUser(@RequestBody UserNewDto dto) {

        registerUseruseCase.execute(dto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                        201,
                        "registro exitoso",
                        authloginUseCase.execute(new AuthRequest(dto.userName(),dto.password()))
                        )
                );
    }
}

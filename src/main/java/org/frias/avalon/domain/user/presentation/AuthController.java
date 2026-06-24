package org.frias.avalon.domain.user.presentation;

import jakarta.validation.Valid;
import org.frias.avalon.core.exeptions.ApiResponse;
import org.frias.avalon.domain.person.application.dto.response.VerificationResponseDto;
import org.frias.avalon.domain.user.application.dtos.request.AuthRequest;
import org.frias.avalon.domain.user.application.dtos.request.TokenRefreshRequest;
import org.frias.avalon.domain.user.application.dtos.request.VerifyUsernameRequestDto;
import org.frias.avalon.domain.user.application.dtos.response.AuthResponse;
import org.frias.avalon.domain.user.application.usecase.accesrefreshtoken.GenerateAccessTokenAndRefreshTokenUseCase;
import org.frias.avalon.domain.user.application.usecase.login.LoginUseCase;
import org.frias.avalon.domain.user.application.usecase.verify.VerifyUsernameUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/avalon/auth")
public class AuthController {

    private final LoginUseCase loginUseCase;
    private final GenerateAccessTokenAndRefreshTokenUseCase refreshTokenUseCase;
    private final VerifyUsernameUseCase verifyUsernameUseCase;

    public AuthController(LoginUseCase loginUseCase, GenerateAccessTokenAndRefreshTokenUseCase refreshTokenUseCase, VerifyUsernameUseCase verifyUsernameUseCase) {
        this.loginUseCase = loginUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
        this.verifyUsernameUseCase = verifyUsernameUseCase;
    }


    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> auth(@Valid @RequestBody AuthRequest credentials) {

        AuthResponse auth = loginUseCase.execute(credentials);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>(
                                200,
                                "Inicio de seccion Exitoso",
                                auth
                        )
                );
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@RequestBody TokenRefreshRequest oldToken) {

        AuthResponse newAccessToken = refreshTokenUseCase.execute(oldToken.refreshToken());

        return ResponseEntity.ok(new ApiResponse<>(
                200,
                "Token de acceso renovado exitosamente",
                newAccessToken
        ));
    }

    @PostMapping("/verify-username")
    public ResponseEntity<ApiResponse<VerificationResponseDto>> verifyUsername(@Valid @RequestBody VerifyUsernameRequestDto request) {
        VerificationResponseDto response = verifyUsernameUseCase.execute(request);
        return ResponseEntity.ok(new ApiResponse<>(
                200,
                "Verificación de nombre de usuario completada",
                response
        ));
    }
}
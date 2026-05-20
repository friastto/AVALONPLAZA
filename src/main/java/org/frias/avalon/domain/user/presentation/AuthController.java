package org.frias.avalon.domain.user.presentation;

import jakarta.validation.Valid;
import org.frias.avalon.core.exeptions.ApiResponse;
import org.frias.avalon.domain.user.application.dtos.request.AuthRequest;
import org.frias.avalon.domain.user.application.dtos.request.TokenRefreshRequest;
import org.frias.avalon.domain.user.application.dtos.response.AuthResponse;
import org.frias.avalon.domain.user.application.dtos.response.TokenRefreshResult;
import org.frias.avalon.domain.user.application.usecase.accesrefreshtoken.AccessRefreshTokenUseCase;
import org.frias.avalon.domain.user.application.usecase.login.LoginUseCase;
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
private final AccessRefreshTokenUseCase refreshTokenUseCase;
    public AuthController(LoginUseCase loginUseCase, AccessRefreshTokenUseCase refreshTokenUseCase) {
        this.loginUseCase = loginUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
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
    public ResponseEntity<ApiResponse<TokenRefreshResult>> refresh(@RequestBody TokenRefreshRequest oldToken) {

        TokenRefreshResult newAccessToken = refreshTokenUseCase.execute(oldToken.refreshToken());

        return ResponseEntity.ok(new ApiResponse<>(
                200,
                "Token de acceso renovado exitosamente",
                newAccessToken
        ));
    }
}

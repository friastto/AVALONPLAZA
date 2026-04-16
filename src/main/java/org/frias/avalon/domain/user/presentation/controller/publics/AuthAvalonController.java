package org.frias.avalon.domain.user.presentation.controller.publics;

import org.frias.avalon.core.exeptions.ApiResponse;
import org.frias.avalon.domain.user.application.usecase.inter.publics.AuthloginUseCase;
import org.frias.avalon.domain.user.domain.dtos.request.AuthRequest;
import org.frias.avalon.domain.user.domain.dtos.response.AuthResponse;
import org.frias.avalon.domain.user.application.authservices.interfaces.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.spec.InvalidKeySpecException;

@RestController
@RequestMapping("/auth-avalon")
public class AuthAvalonController {

    private final AuthService authService;

    private final AuthloginUseCase authloginUseCase;


    public AuthAvalonController(AuthService authService, AuthloginUseCase authloginUseCase) {
        this.authService = authService;

        this.authloginUseCase = authloginUseCase;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody AuthRequest credentials) throws InvalidKeySpecException {

        AuthResponse authResponse = authService.login(credentials);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>(200,"inicio de seccion exitoso",authResponse));
    }

}





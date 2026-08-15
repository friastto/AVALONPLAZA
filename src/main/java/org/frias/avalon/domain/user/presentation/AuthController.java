package org.frias.avalon.domain.user.presentation;

import jakarta.validation.Valid;
import org.frias.avalon.core.exeptions.ApiResponse;
import org.frias.avalon.domain.person.application.dto.response.VerificationResponseDto;
import org.frias.avalon.domain.user.application.dtos.request.*;
import org.frias.avalon.domain.user.application.dtos.response.AuthResponse;
import org.frias.avalon.domain.user.application.dtos.response.ForgotPasswordResponseDto;
import org.frias.avalon.domain.user.application.dtos.response.VerifyPinResponseDto;
import org.frias.avalon.domain.user.application.usecase.accesrefreshtoken.GenerateAccessTokenAndRefreshTokenUseCase;
import org.frias.avalon.domain.user.application.usecase.login.LoginUseCase;
import org.frias.avalon.domain.user.application.usecase.password.ConfirmEmailAndSendPinUseCase;
import org.frias.avalon.domain.user.application.usecase.password.RequestPasswordResetUseCase;
import org.frias.avalon.domain.user.application.usecase.password.ResetPasswordUseCase;
import org.frias.avalon.domain.user.application.usecase.password.VerifyPasswordResetPinUseCase;
import org.frias.avalon.domain.user.application.usecase.verify.VerifyUsernameUseCase;
import org.frias.avalon.domain.user.application.usecase.impersonate.ImpersonateOutletUseCase;
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
    private final RequestPasswordResetUseCase requestPasswordResetUseCase;
    private final ConfirmEmailAndSendPinUseCase confirmEmailAndSendPinUseCase;
    private final VerifyPasswordResetPinUseCase verifyPasswordResetPinUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;
    private final ImpersonateOutletUseCase impersonateOutletUseCase;

    public AuthController(LoginUseCase loginUseCase, GenerateAccessTokenAndRefreshTokenUseCase refreshTokenUseCase, VerifyUsernameUseCase verifyUsernameUseCase, RequestPasswordResetUseCase requestPasswordResetUseCase, ConfirmEmailAndSendPinUseCase confirmEmailAndSendPinUseCase, VerifyPasswordResetPinUseCase verifyPasswordResetPinUseCase, ResetPasswordUseCase resetPasswordUseCase, ImpersonateOutletUseCase impersonateOutletUseCase) {
        this.loginUseCase = loginUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
        this.verifyUsernameUseCase = verifyUsernameUseCase;
        this.requestPasswordResetUseCase = requestPasswordResetUseCase;
        this.confirmEmailAndSendPinUseCase = confirmEmailAndSendPinUseCase;
        this.verifyPasswordResetPinUseCase = verifyPasswordResetPinUseCase;
        this.resetPasswordUseCase = resetPasswordUseCase;
        this.impersonateOutletUseCase = impersonateOutletUseCase;
    }


    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> auth(@Valid @RequestBody AuthRequest credentials) {
        AuthResponse auth = loginUseCase.execute(credentials);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>(200, "Inicio de seccion Exitoso", auth));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@RequestBody TokenRefreshRequest oldToken) {
        AuthResponse newAccessToken = refreshTokenUseCase.execute(oldToken.refreshToken());
        return ResponseEntity.ok(new ApiResponse<>(200, "Token de acceso renovado exitosamente", newAccessToken));
    }

    @PostMapping("/verify-username")
    public ResponseEntity<ApiResponse<VerificationResponseDto>> verifyUsername(@Valid @RequestBody VerifyUsernameRequestDto request) {
        VerificationResponseDto response = verifyUsernameUseCase.execute(request);
        return ResponseEntity.ok(new ApiResponse<>(200, "Verificación de nombre de usuario completada", response));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<ForgotPasswordResponseDto>> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDto request) {
        ForgotPasswordResponseDto response = requestPasswordResetUseCase.execute(request);
        return ResponseEntity.ok(new ApiResponse<>(200, "Pista de correo obtenida.", response));
    }

    @PostMapping("/confirm-email-and-send-pin")
    public ResponseEntity<ApiResponse<String>> confirmEmailAndSendPin(@Valid @RequestBody ConfirmEmailRequestDto request) {
        confirmEmailAndSendPinUseCase.execute(request);
        return ResponseEntity.ok(new ApiResponse<>(200, "Si el correo es correcto, se ha enviado un código de recuperación.", null));
    }

    @PostMapping("/verify-pin")
    public ResponseEntity<ApiResponse<VerifyPinResponseDto>> verifyPin(@Valid @RequestBody VerifyPinRequestDto request) {
        System.out.println(request.toString());
        VerifyPinResponseDto response = verifyPasswordResetPinUseCase.execute(request);

        System.out.println(response.toString());

        return ResponseEntity.ok(new ApiResponse<>(200, "PIN verificado correctamente.", response));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@Valid @RequestBody ResetPasswordRequestDto request) {
        resetPasswordUseCase.execute(request);


        return ResponseEntity.ok(new ApiResponse<>(200, "Su contraseña ha sido restablecida exitosamente.", null));
    }

    @PostMapping("/impersonate/{outletId}")
    public ResponseEntity<ApiResponse<AuthResponse>> impersonate(@org.springframework.web.bind.annotation.PathVariable Long outletId) {
        AuthResponse auth = impersonateOutletUseCase.execute(outletId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>(200, "Suplantación de rol iniciada correctamente", auth));
    }
}
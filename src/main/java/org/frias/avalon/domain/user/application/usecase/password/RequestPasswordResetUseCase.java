package org.frias.avalon.domain.user.application.usecase.password;

import org.frias.avalon.domain.user.application.dtos.request.ForgotPasswordRequestDto;
import org.frias.avalon.domain.user.application.dtos.response.ForgotPasswordResponseDto;

public interface RequestPasswordResetUseCase {
    ForgotPasswordResponseDto execute(ForgotPasswordRequestDto request);
}
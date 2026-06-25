package org.frias.avalon.domain.user.application.usecase.password;

import org.frias.avalon.domain.user.application.dtos.request.VerifyPinRequestDto;
import org.frias.avalon.domain.user.application.dtos.response.VerifyPinResponseDto;

public interface VerifyPasswordResetPinUseCase {
    VerifyPinResponseDto execute(VerifyPinRequestDto request);
}
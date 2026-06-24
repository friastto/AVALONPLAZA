package org.frias.avalon.domain.user.application.usecase.verify;

import org.frias.avalon.domain.person.application.dto.response.VerificationResponseDto;
import org.frias.avalon.domain.user.application.dtos.request.VerifyUsernameRequestDto;

public interface VerifyUsernameUseCase {
    VerificationResponseDto execute(VerifyUsernameRequestDto request);
}
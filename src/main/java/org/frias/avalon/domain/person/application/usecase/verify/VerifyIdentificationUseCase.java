package org.frias.avalon.domain.person.application.usecase.verify;

import org.frias.avalon.domain.person.application.dto.request.VerifyIdentificationRequestDto;
import org.frias.avalon.domain.person.application.dto.response.VerificationResponseDto;

public interface VerifyIdentificationUseCase {
    VerificationResponseDto execute(VerifyIdentificationRequestDto request);
}
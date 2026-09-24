package org.frias.avalon.domain.user.application.usecase.applicant;

import org.frias.avalon.domain.user.application.dtos.request.ApplicantVerifyPinRequestDto;
import org.frias.avalon.domain.user.application.dtos.response.ApplicantVerifiedResponseDto;

public interface ApplicantVerifyPinUseCase {
    ApplicantVerifiedResponseDto execute(ApplicantVerifyPinRequestDto request);
}

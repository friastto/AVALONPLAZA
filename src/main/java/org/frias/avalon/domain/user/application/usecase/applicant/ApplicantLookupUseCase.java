package org.frias.avalon.domain.user.application.usecase.applicant;

import org.frias.avalon.domain.user.application.dtos.request.ApplicantLookupRequestDto;
import org.frias.avalon.domain.user.application.dtos.response.ApplicantLookupResponseDto;

public interface ApplicantLookupUseCase {
    ApplicantLookupResponseDto execute(ApplicantLookupRequestDto request);
}

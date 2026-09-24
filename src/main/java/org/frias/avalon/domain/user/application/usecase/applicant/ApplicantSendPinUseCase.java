package org.frias.avalon.domain.user.application.usecase.applicant;

import org.frias.avalon.domain.user.application.dtos.request.ApplicantSendPinRequestDto;

public interface ApplicantSendPinUseCase {
    void execute(ApplicantSendPinRequestDto request);
}

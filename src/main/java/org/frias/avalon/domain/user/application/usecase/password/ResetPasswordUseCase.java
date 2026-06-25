package org.frias.avalon.domain.user.application.usecase.password;

import org.frias.avalon.domain.user.application.dtos.request.ResetPasswordRequestDto;

public interface ResetPasswordUseCase {
    void execute(ResetPasswordRequestDto request);
}
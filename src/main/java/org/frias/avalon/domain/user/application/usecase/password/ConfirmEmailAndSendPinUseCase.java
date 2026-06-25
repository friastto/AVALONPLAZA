package org.frias.avalon.domain.user.application.usecase.password;

import org.frias.avalon.domain.user.application.dtos.request.ConfirmEmailRequestDto;

public interface ConfirmEmailAndSendPinUseCase {
    void execute(ConfirmEmailRequestDto request);
}
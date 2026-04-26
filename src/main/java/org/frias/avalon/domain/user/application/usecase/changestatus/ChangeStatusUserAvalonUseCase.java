package org.frias.avalon.domain.user.application.usecase.changestatus;

import org.frias.avalon.domain.user.application.dtos.request.ChangeUserAvalonStatusRequest;
import org.frias.avalon.domain.user.application.dtos.response.UserAvalonResponseDto;

public interface ChangeStatusUserAvalonUseCase {

    UserAvalonResponseDto execute(ChangeUserAvalonStatusRequest request);

}

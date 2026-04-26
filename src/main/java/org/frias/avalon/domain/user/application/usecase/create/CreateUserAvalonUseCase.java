package org.frias.avalon.domain.user.application.usecase.create;

import org.frias.avalon.domain.user.application.dtos.request.UserNewDto;
import org.frias.avalon.domain.user.application.dtos.response.UserAvalonResponseDto;

public interface CreateUserAvalonUseCase {
    UserAvalonResponseDto execute(UserNewDto dto);
}

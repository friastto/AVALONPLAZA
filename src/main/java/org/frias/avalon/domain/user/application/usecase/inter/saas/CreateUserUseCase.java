package org.frias.avalon.domain.user.application.usecase.inter.saas;

import org.frias.avalon.domain.user.domain.dtos.request.UserNewDto;
import org.frias.avalon.domain.user.domain.dtos.request.UserNewLinkPersonDto;
import org.frias.avalon.domain.user.domain.dtos.response.UserResponseDto;

public interface CreateUserUseCase {

    UserResponseDto execute(UserNewDto request);
    UserResponseDto execute(UserNewLinkPersonDto dto);
}

package org.frias.avalon.domain.user.application.usecase.inter.company;

import org.frias.avalon.domain.user.domain.dtos.request.UserNewDto;
import org.frias.avalon.domain.user.domain.dtos.response.UserResponseDto;

public interface CreateUserUseCase {

    UserResponseDto execute(UserNewDto request);
}

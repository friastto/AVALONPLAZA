package org.frias.avalon.domain.user.application.usecase.inter.saas;

import org.frias.avalon.domain.user.domain.dtos.request.UserNewDto;
import org.frias.avalon.domain.user.domain.dtos.response.UserResponseDto;

public interface CreateUserCompanyUseCase {

    UserResponseDto execute(UserNewDto request);
}

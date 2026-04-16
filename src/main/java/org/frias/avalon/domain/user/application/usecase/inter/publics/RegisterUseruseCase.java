package org.frias.avalon.domain.user.application.usecase.inter.publics;

import org.frias.avalon.domain.user.domain.dtos.request.UserNewDto;
import org.frias.avalon.domain.user.domain.dtos.response.AuthResponse;

public interface RegisterUseruseCase {

    AuthResponse execute(UserNewDto dto);

}

package org.frias.avalon.domain.user.application.usecase.register;

import org.frias.avalon.domain.user.application.dtos.request.FullPersonAndUser;
import org.frias.avalon.domain.user.application.dtos.response.AuthResponse;

public interface RegisterUserUseCase {
    AuthResponse execute(FullPersonAndUser request);
}
package org.frias.avalon.domain.user.application.usecase.login;

import org.frias.avalon.domain.user.application.dtos.request.AuthRequest;
import org.frias.avalon.domain.user.application.dtos.response.AuthResponse;

public interface LoginUseCase {

    AuthResponse execute(AuthRequest request);


}

package org.frias.avalon.domain.user.application.usecase.inter.publics;

import org.frias.avalon.domain.user.domain.dtos.request.AuthRequest;
import org.frias.avalon.domain.user.domain.dtos.response.AuthResponse;
import org.frias.avalon.domain.user.domain.dtos.response.UserResponseDto;

public interface AuthloginUseCase {

    AuthResponse execute(AuthRequest request);
}

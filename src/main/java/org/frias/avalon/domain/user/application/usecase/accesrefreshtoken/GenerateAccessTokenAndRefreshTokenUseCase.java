package org.frias.avalon.domain.user.application.usecase.accesrefreshtoken;

import org.frias.avalon.domain.user.application.dtos.response.AuthResponse;

public interface GenerateAccessTokenAndRefreshTokenUseCase {
    AuthResponse execute(String tokenStr);

}

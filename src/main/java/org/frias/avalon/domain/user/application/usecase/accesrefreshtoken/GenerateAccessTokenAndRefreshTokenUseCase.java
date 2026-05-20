package org.frias.avalon.domain.user.application.usecase.accesrefreshtoken;

import org.frias.avalon.domain.user.application.dtos.response.AuthResponse;
import org.frias.avalon.domain.user.application.dtos.response.TokenRefreshResult;

public interface GenerateAccessTokenAndRefreshTokenUseCase {
    AuthResponse execute(String tokenStr);

}

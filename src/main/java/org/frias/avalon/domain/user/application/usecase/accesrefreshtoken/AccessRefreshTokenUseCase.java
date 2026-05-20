package org.frias.avalon.domain.user.application.usecase.accesrefreshtoken;

import org.frias.avalon.domain.user.application.dtos.response.TokenRefreshResult;

public interface AccessRefreshTokenUseCase {
    TokenRefreshResult execute(String tokenStr);
}

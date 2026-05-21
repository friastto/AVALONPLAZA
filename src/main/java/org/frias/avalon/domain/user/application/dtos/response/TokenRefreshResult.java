package org.frias.avalon.domain.user.application.dtos.response;

public record TokenRefreshResult(
        String accessToken,
        String refreshToken
) {
}

package org.frias.avalon.domain.user.application.dtos.response;

import org.frias.avalon.domain.user.application.dtos.response.modes.ModesResponseDto;

public record AuthResponse(
        String accessToken, // Renombrado para mayor claridad
        String refreshToken, // Nuevo campo para el refresh token
        UserAvalonResponseDto user,
        ModesResponseDto modes
) {
}

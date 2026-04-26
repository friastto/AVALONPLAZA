package org.frias.avalon.domain.user.application.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

public record AuthResponse(
        String token,
        UserAvalonResponseDto user,
        List<RoleAccessDto> roles
) {}

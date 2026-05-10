package org.frias.avalon.domain.user.application.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.frias.avalon.domain.user.application.dtos.response.modes.ModesResponseDto;

import java.util.List;

public record AuthResponse(
        String token,
        UserAvalonResponseDto user,
       // List<String> roles,
        //List<String> permissions,
        ModesResponseDto modes


) {}

/*
Modes
 */
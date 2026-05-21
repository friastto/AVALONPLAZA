package org.frias.avalon.domain.user.application.dtos.response.modes;

import java.util.List;

public record ClientModeDto(
        String type,
        boolean enabled,
        List<String> permissions
) {
}

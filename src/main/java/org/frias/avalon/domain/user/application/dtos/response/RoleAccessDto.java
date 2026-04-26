package org.frias.avalon.domain.user.application.dtos.response;

import java.util.List;

public record RoleAccessDto(
        String roleCode,
        String roleName,
        List<String> permissions
) {}
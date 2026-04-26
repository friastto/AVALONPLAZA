package org.frias.avalon.domain.user.application.dtos.request;

import java.util.List;

public record AuthorizationResult(
        List<String> roles,
        List<String> permissions,
        List<String> scopes
) {}
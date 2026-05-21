package org.frias.avalon.domain.user.application.dtos.results;

import java.util.List;

public record ClientResult(
        String type,
        boolean status,
        List<String> permissions
) {
}

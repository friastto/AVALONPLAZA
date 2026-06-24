package org.frias.avalon.domain.user.application.dtos.request;

import org.frias.avalon.domain.person.application.dto.request.CreatePersonRequest;

public record FullPersonAndUser(
        CreatePersonRequest person,
        UserNewDto userAvalon,
        Long roleId,
        Long outletId
) {
}
package org.frias.avalon.domain.user.domain.dtos.request;

public record UserNewLinkPersonDto(
        String userName,
        String password,
        Long roleId,
        Long personId,
        Long companyId,
        Long outletId
) {}

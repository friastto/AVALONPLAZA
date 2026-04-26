package org.frias.avalon.domain.user.application.dtos.request;

public record UserNewLinkPersonDto(
        String userName,
        String password,
        Long roleId,
        Long companyId,

        Long personId,
        Long outletId
) implements BaseNewUserDto{}

package org.frias.avalon.domain.user.domain.dtos.request;

import org.frias.avalon.domain.person.dto.PersonRequestNewDto;

public record UserNewDto(

    String userName,
    String password,
    Long roleId,
    Long companyId,
    PersonRequestNewDto newPersonData,

    Long outletId

) implements BaseNewUserDto{}

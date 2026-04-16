package org.frias.avalon.domain.user.domain.dtos.request;

import org.frias.avalon.domain.person.dto.PersonRequestNewDto;

public record UserNewDto(

    String userName,
    String password,
    Long role,
    PersonRequestNewDto newPersonData,
    Long companyId,
    Long outletId

){
}

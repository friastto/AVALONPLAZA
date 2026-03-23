package org.frias.avalon.domain.usergeneral.useravalon.dtos;

import org.frias.avalon.temp.person.dto.PersonRequestNewDto;

public record UserRequestNewDto(

    String userName,
    String password,
    Long role,

    PersonRequestNewDto personId

){
}

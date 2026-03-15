package org.frias.avalon.useravalon.dtos;

import org.frias.avalon.person.dto.PersonRequestNewDto;

public record UserRequestNewDto(

    String userName,
    String password,

    PersonRequestNewDto personId

){
}

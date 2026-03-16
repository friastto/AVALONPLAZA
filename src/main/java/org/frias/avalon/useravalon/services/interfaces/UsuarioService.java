package org.frias.avalon.useravalon.services.interfaces;

import org.frias.avalon.useravalon.dtos.UserLinkPersonRequestDto;
import org.frias.avalon.useravalon.dtos.UserRequestNewDto;
import org.frias.avalon.useravalon.dtos.UserResponseDto;
import org.frias.avalon.useravalon.entities.UserAvalon;

import java.security.spec.InvalidKeySpecException;

public interface UsuarioService {

    UserAvalon searchByUserName(String userName);
    UserAvalon getUserEmployeeStatus(String numberId);

    Boolean saveUserAndPerson(UserRequestNewDto userCreate);

    Boolean saveUserAndCreateLinkPerson(UserLinkPersonRequestDto userCreate) throws InvalidKeySpecException;


}

package org.frias.avalon.domain.usergeneral.useravalon.services.interfaces;

import org.frias.avalon.domain.usergeneral.useravalon.dtos.UserLinkPersonRequestDto;
import org.frias.avalon.domain.usergeneral.useravalon.dtos.UserRequestNewDto;
import org.frias.avalon.domain.usergeneral.useravalon.entities.UserAvalon;

import java.security.spec.InvalidKeySpecException;

public interface UsersService {

    UserAvalon searchByUserName(String userName);
    UserAvalon getUserEmployeeStatus(String numberId);

    UserAvalon searchById(Long Id);

    UserAvalon saveUserAndPerson(UserRequestNewDto userCreate);

    UserAvalon saveUserAndCreateLinkPerson(UserLinkPersonRequestDto userCreate) throws InvalidKeySpecException;

    UserAvalon clear(Long id);

    UserAvalon changeStatus(Long idUser, Long idStatus);
}

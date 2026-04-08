package org.frias.avalon.domain.usergeneral.useravalon.services.interfaces;

import org.frias.avalon.domain.usergeneral.useravalon.dtos.request.UserNewDto;
import org.frias.avalon.domain.usergeneral.useravalon.dtos.request.UserNewLinkPersonDto;
import org.frias.avalon.domain.usergeneral.useravalon.entities.UserAvalon;

import java.security.spec.InvalidKeySpecException;

public interface UsersService {

    UserAvalon create(UserNewLinkPersonDto request);

    UserAvalon createUserAndCreateLinkPerson(UserNewLinkPersonDto userCreate) throws InvalidKeySpecException;

    UserAvalon createUserAndPerson(UserNewDto userCreate);

    UserAvalon searchById(Long Id);

    UserAvalon searchByUserName(String userName);

    UserAvalon clear(Long id);

    UserAvalon changeStatus(Long idUser, Long idStatus);



}

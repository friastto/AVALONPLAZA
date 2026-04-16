package org.frias.avalon.domain.user.domain.services.interfaces;

import org.frias.avalon.domain.user.domain.dtos.request.UserNewDto;
import org.frias.avalon.domain.user.domain.dtos.request.UserNewLinkPersonDto;
import org.frias.avalon.domain.user.domain.entities.UserAvalon;

import java.security.spec.InvalidKeySpecException;

public interface UsersService {

    UserAvalon create(UserNewLinkPersonDto request);

    UserAvalon createUserAndCreateLinkPerson(UserNewLinkPersonDto userCreate) throws InvalidKeySpecException;

    UserAvalon createUserAndPerson(UserNewDto userCreate);

    UserAvalon searchById(Long Id);

    UserAvalon searchByUserName(String userName);

    UserAvalon clear(Long id);

    UserAvalon changeStatus(Long idUser, Long idStatus);


    UserAvalon create(
            Long idCompany,
            String name,
            String password,
            Long role);
}

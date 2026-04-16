package org.frias.avalon.domain.user.domain.services.interfaces;

import org.frias.avalon.domain.person.entity.Person;
import org.frias.avalon.domain.user.domain.dtos.request.UserNewDto;
import org.frias.avalon.domain.user.domain.dtos.request.UserNewLinkPersonDto;
import org.frias.avalon.domain.user.domain.entities.UserAvalon;

import java.security.spec.InvalidKeySpecException;
import java.util.List;

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

    UserAvalon createUserWithRules(
            Long companyId,
            String operatorRol,
            String username,
            String password,
            Long role);


    UserAvalon createUser(Long aLong, String rol, Person person, String s, String password, Long aLong1);

    boolean existsByPersonAndRole(Long personId, Long roleId);

    List<UserAvalon> getAllUserIntoCompany(Long idCompany);
}

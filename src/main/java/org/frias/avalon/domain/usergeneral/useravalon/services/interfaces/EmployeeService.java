package org.frias.avalon.domain.usergeneral.useravalon.services.interfaces;

import org.frias.avalon.domain.usergeneral.useravalon.dtos.request.UserNewDto;
import org.frias.avalon.domain.usergeneral.useravalon.dtos.request.UserNewLinkPersonDto;
import org.frias.avalon.domain.usergeneral.useravalon.entities.UserAvalon;

import java.security.spec.InvalidKeySpecException;
import java.util.List;

public interface EmployeeService {

    UserAvalon createUserAndCreateLinkPerson(UserNewLinkPersonDto userCreate) throws InvalidKeySpecException;
    UserAvalon createUserAndPerson(UserNewDto userCreate);
    UserAvalon searchById(Long Id);
    UserAvalon searchByUserName(String userName);
    UserAvalon clear(Long id);
    UserAvalon changeStatus(Long idUser, Long idStatus);

    List<UserAvalon> getAllEmployeesOnlyCompany(Long idCompany);
    List<UserAvalon> getAllEmployeesOnlyOutlet(Long idOutlet);
    List<UserAvalon> getAll(Long idCompany);
}

package org.frias.avalon.temp.features.tenant.users.services.interfaces;

import org.frias.avalon.domain.usergeneral.useravalon.dtos.UserLinkPersonRequestDto;
import org.frias.avalon.domain.usergeneral.useravalon.dtos.UserRequestNewDto;
import org.frias.avalon.domain.usergeneral.useravalon.entities.UserAvalon;

public interface EmployeeCompanyService {

    Boolean validateEmployee(Long id);

    UserAvalon createUserEmployee(UserRequestNewDto userRequestNewDto);

    UserAvalon changeStatusToEmployee(Long idEmployee,Long idStatus);

    UserAvalon clear(Long idEmployee);

    UserAvalon createUserLinkToPerson(UserLinkPersonRequestDto dto);

}

package org.frias.avalon.temp.features.tenant.users.services.implementations;

import jakarta.persistence.EntityNotFoundException;
import org.frias.avalon.temp.empresasucursal.tenant.tenantcontex.TenantContext;
import org.frias.avalon.temp.features.tenant.users.services.interfaces.EmployeeCompanyService;
import org.frias.avalon.domain.usergeneral.useravalon.dtos.UserLinkPersonRequestDto;
import org.frias.avalon.domain.usergeneral.useravalon.dtos.UserRequestNewDto;
import org.frias.avalon.domain.usergeneral.useravalon.entities.UserAvalon;
import org.frias.avalon.domain.usergeneral.useravalon.services.interfaces.UsersService;
import org.frias.avalon.domain.masterdata.services.interfaces.MasterDataService;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.security.spec.InvalidKeySpecException;

public class EmployeeCompanyServiceImpl implements EmployeeCompanyService {

    private final UsersService usersService;
    private final MasterDataService masterDataService;

    public EmployeeCompanyServiceImpl(UsersService usersService, MasterDataService masterDataService) {
        this.usersService = usersService;
        this.masterDataService = masterDataService;
    }


    @Override
    public Boolean validateEmployee(Long id) {
        UserAvalon ua;

        try {
             ua = usersService.searchById(id);
        }catch (EntityNotFoundException enfe){
            return false; //throw new SecurityException("usuario corrupto no puede realizar esta accion");
        }
       Long idCompanyContext = TenantContext.getTenantId();

       if((ua != null) && ua.getCompanyId().getId().equals(idCompanyContext)){

          return true;
       }

        return false;
    }

    @Override
    public UserAvalon createUserEmployee(UserRequestNewDto userRequestNewDto) {

       return  usersService.saveUserAndPerson(userRequestNewDto);
    }



    @Override
    public UserAvalon changeStatusToEmployee(Long idEmployee, Long idStatus) {

       return usersService.changeStatus(idEmployee,idStatus);

    }

   @Override
    public UserAvalon clear(Long id) {

     return usersService.clear(id);
    }

    @Override
    public UserAvalon createUserLinkToPerson(UserLinkPersonRequestDto dto) {

        try {

          return  usersService.saveUserAndCreateLinkPerson(dto);

        } catch (InvalidKeySpecException e) {

            // Devuelve un error 400 o 500 según corresponda, sin ensuciar la firma
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error de encriptación", e);
        }
    }


}

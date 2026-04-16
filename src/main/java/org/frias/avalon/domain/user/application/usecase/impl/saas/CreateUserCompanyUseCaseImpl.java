package org.frias.avalon.domain.user.application.usecase.impl.saas;

import org.frias.avalon.domain.company.facade.TenantSecurity;
import org.frias.avalon.domain.user.application.usecase.inter.saas.CreateUserCompanyUseCase;
import org.frias.avalon.domain.user.domain.dtos.request.UserNewDto;
import org.frias.avalon.domain.user.domain.dtos.response.UserResponseDto;
import org.frias.avalon.domain.user.domain.entities.UserAvalon;
import org.frias.avalon.domain.user.domain.services.interfaces.UsersService;
import org.springframework.stereotype.Service;

@Service
public class CreateUserCompanyUseCaseImpl extends TenantSecurity implements CreateUserCompanyUseCase {
    private final UsersService usersService;

    public CreateUserCompanyUseCaseImpl(UsersService usersService) {
        this.usersService = usersService;
    }

    @Override
    public UserResponseDto execute(UserNewDto request) {

        if (isMasterStaff()) throw new SecurityException("No tiene los permisos para crearle un usuario a una empresa de Avalon");

        if (request.companyId() == null) throw new SecurityException("no le asigno empresa a al usuario a registrar");

        UserAvalon userAvalonCreated = usersService.create(request.companyId(),request.userName(),request.password(),request.role());

        return new UserResponseDto(userAvalonCreated.getId(),userAvalonCreated.getUserName(),userAvalonCreated.getRolId().getShortName(),null);
    }
}

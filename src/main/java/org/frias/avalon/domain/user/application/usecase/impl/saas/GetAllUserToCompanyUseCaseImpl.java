package org.frias.avalon.domain.user.application.usecase.impl.saas;

import org.frias.avalon.domain.user.application.usecase.inter.saas.GetAllUserToCompanyUseCase;
import org.frias.avalon.domain.user.domain.dtos.response.UserAvalonDto;
import org.frias.avalon.domain.user.domain.dtos.response.UserResponseDto;
import org.frias.avalon.domain.user.domain.entities.UserAvalon;
import org.frias.avalon.domain.user.domain.services.interfaces.UsersService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetAllUserToCompanyUseCaseImpl implements GetAllUserToCompanyUseCase {
    private final UsersService usersService;

    public GetAllUserToCompanyUseCaseImpl(UsersService usersService) {
        this.usersService = usersService;
    }

    @Override
    public List<UserAvalonDto> execute(Long idCompany) {

        List<UserAvalon> userCompany  = usersService.getAllUserIntoCompany(idCompany);


        return userCompany.stream()
                .map(uc -> new UserAvalonDto(
                        uc.getId(),
                        uc.getPerson().getIdentificationId().getShortName(),
                        uc.getPerson().getNumberid(),
                        uc.getUserName(),
                        uc.getRolId().getShortName(),
                        uc.getPerson().getName()  +" "+ uc.getPerson().getLastName(),
                        uc.getPerson().getDir(),
                        uc.getPerson().getSexId().getShortName()
                        )
                )
                .toList(); // Java 16+
    }
}

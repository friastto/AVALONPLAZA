package org.frias.avalon.domain.user.application.usecase.impl.saas;

import org.frias.avalon.domain.company.facade.TenantSecurity;
import org.frias.avalon.domain.person.entity.Person;
import org.frias.avalon.domain.person.services.interfaces.PersonService;
import org.frias.avalon.domain.user.application.usecase.inter.saas.CreateUserUseCase;
import org.frias.avalon.domain.user.domain.dtos.request.BaseNewUserDto;
import org.frias.avalon.domain.user.domain.dtos.request.UserNewDto;
import org.frias.avalon.domain.user.domain.dtos.request.UserNewLinkPersonDto;
import org.frias.avalon.domain.user.domain.dtos.response.UserResponseDto;
import org.frias.avalon.domain.user.domain.entities.UserAvalon;
import org.frias.avalon.domain.user.domain.services.interfaces.UsersService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateUserUseCaseImpl extends TenantSecurity implements CreateUserUseCase {
    private final UsersService usersService;
    private final PersonService personService;

    public CreateUserUseCaseImpl(UsersService usersService, PersonService personService) {
        this.usersService = usersService;
        this.personService = personService;
    }

    @Override
    @Transactional
    public UserResponseDto execute(UserNewDto dto) {

        Person person = personService.create(
                dto.newPersonData().numberId(),
                dto.newPersonData().name(),
                dto.newPersonData().lastName(),
                dto.newPersonData().address(),
                dto.newPersonData().identificationId(),
                dto.newPersonData().sexId()
        );
        return createUserInternal(person,dto);

    }
    @Transactional
    public UserResponseDto execute(UserNewLinkPersonDto dto) {

        Person person = personService.searchById(dto.personId());

        return createUserInternal(person, dto);
    }


    private UserResponseDto createUserInternal(Person person, BaseNewUserDto dto) {

        UserAvalon user = usersService.createUser(
                resolveCompanyId(dto),
                getRol(), //este rol se obtiene del contexto de seguridad
                person,
                dto.userName(),
                dto.password(),
                dto.roleId()
        );

        return new UserResponseDto(user.getId(),user.getUserName(),user.getRolId().getShortName(),user.getCompanyId().getId());
    }

    private Long resolveCompanyId(BaseNewUserDto dto) {

        if (isMasterStaff()) {
            // SaaS → puede elegir empresa
            if (dto.companyId() == null) {
                throw new IllegalArgumentException("Debe indicar la empresa");
            }
            return dto.companyId();
        }

        // Empresa → se usa su propio tenant
        Long companyId = getCompanyId();

        if (companyId == null) {
            throw new SecurityException("No tienes empresa asociada");
        }

        return companyId;
    }
}

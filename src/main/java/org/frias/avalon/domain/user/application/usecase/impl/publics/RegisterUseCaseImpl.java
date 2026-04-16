package org.frias.avalon.domain.user.application.usecase.impl.publics;

import org.frias.avalon.domain.masterdata.entities.MasterData;
import org.frias.avalon.domain.masterdata.services.interfaces.MasterDataService;
import org.frias.avalon.domain.person.dto.PersonRequestNewDto;
import org.frias.avalon.domain.person.entity.Person;
import org.frias.avalon.domain.person.services.interfaces.PersonService;
import org.frias.avalon.domain.user.application.authservices.interfaces.AuthService;
import org.frias.avalon.domain.user.application.usecase.inter.publics.RegisterUseruseCase;
import org.frias.avalon.domain.user.domain.dtos.request.UserNewDto;
import org.frias.avalon.domain.user.domain.dtos.response.AuthResponse;
import org.frias.avalon.domain.user.domain.dtos.response.UserResponseDto;
import org.frias.avalon.domain.user.domain.entities.UserAvalon;
import org.frias.avalon.domain.user.domain.services.interfaces.UsersService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RegisterUseCaseImpl implements RegisterUseruseCase {

    private final UsersService usersService;
    private final PersonService personService;
    private final MasterDataService masterDataService;
    private final AuthService authService;

    public RegisterUseCaseImpl(UsersService usersService,
                               PersonService personService,
                               MasterDataService masterDataService, AuthService authService) {
        this.usersService = usersService;
        this.personService = personService;
        this.masterDataService = masterDataService;
        this.authService = authService;
    }

    @Override
    @Transactional
    public AuthResponse execute(UserNewDto dto) {

            // Buscar o crear persona (IDENTIDAD GLOBAL)
            Person person = resolvePerson(dto.newPersonData());

            // Obtener rol permitido para público
            MasterData role = masterDataService.searchByShortName("INVITADO");

            // Validar que no tenga ya un usuario público
            validatePublicUserDoesNotExist(person.getId(), role.getId());

            //  Crear usuario (SIN empresa, SIN operador)
            UserAvalon user = usersService.createUser(
                    null, // público → sin empresa
                    null, //  no hay operador
                    person,
                    dto.userName(),
                    dto.password(),
                    role.getId() // rol de publico
            );

            return authService.login(dto.userName(), dto.password());
        }


        private Person resolvePerson(PersonRequestNewDto dto) {

            return personService.findByNumberId(dto.numberId())
                    .orElseGet(() -> personService.create(
                            dto.numberId(),
                            dto.name(),
                            dto.lastName(),
                            dto.address(),
                            dto.identificationId(),
                            dto.sexId()
                    ));
        }


        private void validatePublicUserDoesNotExist(Long personId, Long roleId) {

            boolean exists = usersService.existsByPersonAndRole(personId, roleId);

            if (exists) {
                throw new IllegalStateException(
                        "Ya existe un usuario público asociado a esta persona"
                );
            }
        }


        private UserResponseDto mapToResponse(UserAvalon user) {
            return new UserResponseDto(
                    user.getId(),
                    user.getUserName(),
                    user.getRolId().getShortName(),
                    null
            );
        }


}

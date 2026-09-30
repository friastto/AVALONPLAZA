package org.frias.avalon.domain.user.application.usecase.profile;

import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.core.jwt.util.SecurityUtils;
import org.frias.avalon.domain.masterdata.application.dto.response.MasterRefDto;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.person.domain.model.PersonDomain;
import org.frias.avalon.domain.person.domain.port.PersonRepositoryPort;
import org.frias.avalon.domain.user.application.dtos.response.UserProfileResponseDto;
import org.frias.avalon.domain.user.domain.model.RoleAssignmentDomain;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.frias.avalon.domain.user.domain.port.RoleAssignmentRepositoryPort;
import org.frias.avalon.domain.user.domain.port.UserAvalonRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class GetUserProfileUseCaseImpl implements GetUserProfileUseCase {

    private final UserAvalonRepositoryPort userPort;
    private final PersonRepositoryPort personPort;
    private final RoleAssignmentRepositoryPort roleAssignmentPort;
    private final MasterTreeProvider masterTreeProvider;

    public GetUserProfileUseCaseImpl(
            UserAvalonRepositoryPort userPort,
            PersonRepositoryPort personPort,
            RoleAssignmentRepositoryPort roleAssignmentPort,
            MasterTreeProvider masterTreeProvider
    ) {
        this.userPort = userPort;
        this.personPort = personPort;
        this.roleAssignmentPort = roleAssignmentPort;
        this.masterTreeProvider = masterTreeProvider;
    }

    @Override
    public UserProfileResponseDto execute(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            throw new IllegalArgumentException("El identificador de usuario es obligatorio");
        }

        UserAvalonDomain user = findUser(identifier.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con identificador: " + identifier));

        return buildProfile(user);
    }

    @Override
    public UserProfileResponseDto executeCurrent() {
        String currentLogin = SecurityUtils.getCurrentUserLogin();
        if (currentLogin == null || currentLogin.isBlank()) {
            throw new ResourceNotFoundException("No se encontro un usuario autenticado en la sesion actual");
        }
        return execute(currentLogin);
    }

    private Optional<UserAvalonDomain> findUser(String identifier) {
        if (identifier.matches("^\\d+$")) {
            try {
                Long id = Long.parseLong(identifier);
                Optional<UserAvalonDomain> byId = userPort.findById(id);
                if (byId.isPresent()) {
                    return byId;
                }
            } catch (NumberFormatException ignored) {
            }
        }

        Optional<UserAvalonDomain> byUserName = userPort.findByUserName(identifier);
        if (byUserName.isPresent()) {
            return byUserName;
        }

        return userPort.findByIdentifier(identifier);
    }

    private UserProfileResponseDto buildProfile(UserAvalonDomain user) {
        PersonDomain person = null;
        if (user.getPersonId() != null) {
            person = personPort.findById(user.getPersonId()).orElse(null);
        }

        MasterTree tree = masterTreeProvider.getTree();

        MasterRefDto statusRef = null;
        if (user.getStatusId() != null) {
            MasterRoot statusNode = tree.getById(user.getStatusId());
            statusRef = MasterRefDto.from(statusNode);
        }

        MasterRefDto idTypeRef = null;
        if (person != null && person.getTypeIdentificationId() != null) {
            MasterRoot idTypeNode = tree.getById(person.getTypeIdentificationId());
            idTypeRef = MasterRefDto.from(idTypeNode);
        }

        List<MasterRefDto> roles = new ArrayList<>();
        MasterRefDto activeRole = null;

        List<RoleAssignmentDomain> assignments = roleAssignmentPort.findByUserAvalonId(user.getId());
        if (assignments != null) {
            for (RoleAssignmentDomain assignment : assignments) {
                if (assignment.getStatus() != null) {
                    MasterRoot assignStatusNode = tree.getById(assignment.getStatus());
                    if (assignStatusNode != null && tree.is(assignStatusNode, "ACT")) {
                        MasterRoot roleNode = tree.getById(assignment.getRoleId());
                        if (roleNode != null) {
                            MasterRefDto roleRef = MasterRefDto.from(roleNode);
                            roles.add(roleRef);
                            if (activeRole == null) {
                                activeRole = roleRef;
                            }
                        }
                    }
                }
            }
        }

        String name = person != null ? person.getName() : null;
        String lastName = person != null ? person.getLastName() : null;
        String fullName = person != null ? person.getFullName() : user.getUserName();
        String email = person != null ? person.getEmail() : null;
        Long phoneNumber = person != null ? person.getPhoneNumber() : null;
        String address = person != null ? person.getAddress() : null;
        String numberId = person != null ? person.getNumberid() : null;

        return new UserProfileResponseDto(
                user.getId(),
                user.getUserName(),
                user.getPersonId(),
                name,
                lastName,
                fullName,
                email,
                phoneNumber,
                address,
                idTypeRef,
                numberId,
                statusRef,
                roles,
                activeRole
        );
    }
}

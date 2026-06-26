package org.frias.avalon.domain.person.application.usecase.find;

import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.outlet.domain.model.OutletDomain;
import org.frias.avalon.domain.outlet.domain.port.OutletRepositoryPort;
import org.frias.avalon.domain.person.application.dto.response.PersonDetailResponseDto;
import org.frias.avalon.domain.person.domain.model.PersonDomain;
import org.frias.avalon.domain.person.domain.port.PersonRepositoryPort;
import org.frias.avalon.domain.user.domain.model.RoleAssignmentDomain;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.frias.avalon.domain.user.domain.port.RoleAssignmentRepositoryPort;
import org.frias.avalon.domain.user.domain.port.UserAvalonRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FindPersonByDocumentUseCaseImpl implements FindPersonByDocumentUseCase {

    private final PersonRepositoryPort personRepositoryPort;
    private final UserAvalonRepositoryPort userAvalonRepositoryPort;
    private final RoleAssignmentRepositoryPort roleAssignmentRepositoryPort;
    private final OutletRepositoryPort outletRepositoryPort;
    private final MasterTreeProvider masterTreeProvider;

    public FindPersonByDocumentUseCaseImpl(
            PersonRepositoryPort personRepositoryPort,
            UserAvalonRepositoryPort userAvalonRepositoryPort,
            RoleAssignmentRepositoryPort roleAssignmentRepositoryPort,
            OutletRepositoryPort outletRepositoryPort,
            MasterTreeProvider masterTreeProvider) {
        this.personRepositoryPort = personRepositoryPort;
        this.userAvalonRepositoryPort = userAvalonRepositoryPort;
        this.roleAssignmentRepositoryPort = roleAssignmentRepositoryPort;
        this.outletRepositoryPort = outletRepositoryPort;
        this.masterTreeProvider = masterTreeProvider;
    }

    @Override
    public PersonDetailResponseDto execute(String numberid) {

        // 1. Buscar persona por número de documento
        Optional<PersonDomain> personOpt = personRepositoryPort.findByNumberid(numberid);

        if (personOpt.isEmpty()) {
            return new PersonDetailResponseDto(
                    false, false,
                    null, null, null, null, null, null,
                    null, null, null, null,
                    null, null,
                    false, null, null, null, null, null
            );
        }

        PersonDomain person = personOpt.get();

        // Obtener nombres de tipo identificación y sexo del MasterTree
        var tree = masterTreeProvider.getTree();
        MasterRoot typeIdRoot = tree.getById(person.getTypeIdentificationId());
        MasterRoot sexRoot = tree.getById(person.getSexId());

        String typeIdentificationName = typeIdRoot != null ? typeIdRoot.getFullName() : null;
        String sexName = sexRoot != null ? sexRoot.getFullName() : null;

        // 2. Buscar usuario vinculado a esa persona por numberid
        Optional<UserAvalonDomain> userOpt = userAvalonRepositoryPort.findByPersonNumberid(numberid);

        if (userOpt.isEmpty()) {
            // Persona existe pero no tiene usuario
            return new PersonDetailResponseDto(
                    true, false,
                    person.getId(),
                    person.getName(),
                    person.getLastName(),
                    person.getAddress(),
                    person.getEmail(),
                    person.getPhoneNumber(),
                    person.getTypeIdentificationId(),
                    typeIdentificationName,
                    person.getSexId(),
                    sexName,
                    null, null,
                    false, null, null, null, null, null
            );
        }

        UserAvalonDomain user = userOpt.get();

        // 3. Obtener asignaciones de rol del usuario
        List<RoleAssignmentDomain> assignments = roleAssignmentRepositoryPort.findByUserAvalonId(user.getId());

        if (assignments.isEmpty()) {
            // Tiene usuario pero sin rol asignado
            return new PersonDetailResponseDto(
                    true, true,
                    person.getId(),
                    person.getName(),
                    person.getLastName(),
                    person.getAddress(),
                    person.getEmail(),
                    person.getPhoneNumber(),
                    person.getTypeIdentificationId(),
                    typeIdentificationName,
                    person.getSexId(),
                    sexName,
                    user.getId(),
                    user.getUserName(),
                    false, null, null, null, null, null
            );
        }

        // 4. Tomar la asignación más reciente (último elemento de la lista)
        RoleAssignmentDomain latestAssignment = assignments.get(assignments.size() - 1);

        MasterRoot roleRoot = tree.getById(latestAssignment.getRoleId());
        String currentRoleName = roleRoot != null ? roleRoot.getFullName() : null;

        // 5. Obtener nombre del outlet
        String currentOutletName = null;
        if (latestAssignment.getOutletId() != null) {
            Optional<OutletDomain> outletOpt = outletRepositoryPort.findById(latestAssignment.getOutletId());
            currentOutletName = outletOpt.map(OutletDomain::getName).orElse(null);
        }

        return new PersonDetailResponseDto(
                true, true,
                person.getId(),
                person.getName(),
                person.getLastName(),
                person.getAddress(),
                person.getEmail(),
                person.getPhoneNumber(),
                person.getTypeIdentificationId(),
                typeIdentificationName,
                person.getSexId(),
                sexName,
                user.getId(),
                user.getUserName(),
                true,
                currentRoleName,
                latestAssignment.getRoleId(),
                latestAssignment.getOutletId(),
                currentOutletName,
                latestAssignment.getId()
        );
    }
}

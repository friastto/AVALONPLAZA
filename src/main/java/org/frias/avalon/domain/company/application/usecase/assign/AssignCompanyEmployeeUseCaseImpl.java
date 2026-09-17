package org.frias.avalon.domain.company.application.usecase.assign;

import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.domain.company.application.dto.request.AssignCompanyEmployeeRequest;
import org.frias.avalon.domain.company.application.dto.response.CompanyEmployeeResponse;
import org.frias.avalon.domain.company.domain.model.CompanyDomain;
import org.frias.avalon.domain.company.domain.port.CompanyRepositoryPort;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.outlet.domain.model.OutletDomain;
import org.frias.avalon.domain.outlet.domain.port.OutletRepositoryPort;
import org.frias.avalon.domain.person.domain.model.PersonDomain;
import org.frias.avalon.domain.person.domain.port.PersonRepositoryPort;
import org.frias.avalon.core.validation.PassSecure;
import org.frias.avalon.domain.user.domain.model.RoleAssignmentDomain;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.frias.avalon.domain.user.domain.port.RoleAssignmentRepositoryPort;
import org.frias.avalon.domain.user.domain.port.UserAvalonRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AssignCompanyEmployeeUseCaseImpl implements AssignCompanyEmployeeUseCase {

    private final CompanyRepositoryPort companyRepositoryPort;
    private final OutletRepositoryPort outletRepositoryPort;
    private final PersonRepositoryPort personRepositoryPort;
    private final UserAvalonRepositoryPort userAvalonRepositoryPort;
    private final RoleAssignmentRepositoryPort roleAssignmentRepositoryPort;
    private final MasterTreeProvider masterTreeProvider;

    public AssignCompanyEmployeeUseCaseImpl(
            CompanyRepositoryPort companyRepositoryPort,
            OutletRepositoryPort outletRepositoryPort,
            PersonRepositoryPort personRepositoryPort,
            UserAvalonRepositoryPort userAvalonRepositoryPort,
            RoleAssignmentRepositoryPort roleAssignmentRepositoryPort,
            MasterTreeProvider masterTreeProvider
    ) {
        this.companyRepositoryPort = companyRepositoryPort;
        this.outletRepositoryPort = outletRepositoryPort;
        this.personRepositoryPort = personRepositoryPort;
        this.userAvalonRepositoryPort = userAvalonRepositoryPort;
        this.roleAssignmentRepositoryPort = roleAssignmentRepositoryPort;
        this.masterTreeProvider = masterTreeProvider;
    }

    @Override
    @Transactional
    public CompanyEmployeeResponse execute(Long companyId, AssignCompanyEmployeeRequest request) {
        MasterTree tree = masterTreeProvider.getTree();
        MasterRoot activeStatus = tree.getByCodeOrThrow("ACT");
        Long activeStatusId = activeStatus.getId();

        MasterRoot roleRoot = tree.getByIdOrThrow(request.roleId());

        CompanyDomain company = companyRepositoryPort.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada con ID: " + companyId));

        OutletDomain outlet = null;
        if (request.outletId() != null) {
            outlet = outletRepositoryPort.findById(request.outletId())
                    .orElseThrow(() -> new ResourceNotFoundException("Tienda no encontrada con ID: " + request.outletId()));
        }

        // 1. Find or create Person
        Optional<PersonDomain> personOpt = personRepositoryPort.findByNumberid(request.numberid());
        PersonDomain person;
        if (personOpt.isPresent()) {
            person = personOpt.get();
        } else {
            PersonDomain newPerson = PersonDomain.createBasic(
                    request.typeIdentificationId(),
                    request.numberid(),
                    request.name(),
                    request.lastName(),
                    request.address(),
                    request.sexId(),
                    request.phoneNumber(),
                    request.email(),
                    activeStatusId
            );
            person = personRepositoryPort.save(newPerson);
        }

        // 2. Find or create User
        Optional<UserAvalonDomain> userOpt = userAvalonRepositoryPort.findByPersonNumberid(request.numberid());
        UserAvalonDomain user;
        if (userOpt.isPresent()) {
            user = userOpt.get();
        } else {
            String userName = (request.userName() != null && !request.userName().isBlank())
                    ? request.userName()
                    : (request.name().toLowerCase().replaceAll("\\s+", "") + "." + request.numberid());
            String rawPassword = (request.password() != null && !request.password().isBlank())
                    ? request.password()
                    : ("Avalon" + request.numberid() + "*");
            String salt = PassSecure.generateSalt();
            String hashedPassword = PassSecure.hashPassword(rawPassword, salt);

            UserAvalonDomain newUser = UserAvalonDomain.createWithPerson(
                    userName,
                    salt,
                    hashedPassword,
                    activeStatusId,
                    person.getId()
            );
            user = userAvalonRepositoryPort.save(newUser);
        }

        // 3. Create or update Role Assignment using MasterTree role code validation
        boolean isCorporateRole = tree.is(roleRoot, "GERGEN");
        RoleAssignmentDomain assignment;
        if (isCorporateRole) {
            assignment = RoleAssignmentDomain.createCompanyRole(
                    user.getId(),
                    roleRoot.getId(),
                    companyId,
                    activeStatusId
            );
        } else {
            assignment = RoleAssignmentDomain.create(
                    user.getId(),
                    roleRoot.getId(),
                    request.outletId(),
                    activeStatusId
            );
        }
        roleAssignmentRepositoryPort.create(assignment);

        // 4. Build response from MasterTree metadata
        String roleCode = roleRoot.getShortName();
        String roleName = roleRoot.getFullName();

        String category = "OPERATIVO";
        if (roleRoot.getParentId() != null) {
            MasterRoot parent = tree.getById(roleRoot.getParentId());
            if (parent != null) {
                category = parent.getShortName();
            }
        }

        String outletName = outlet != null ? outlet.getName() : "Sede Corporativa / Empresa";
        String statusName = activeStatus != null ? activeStatus.getFullName() : "ACTIVO";

        MasterRoot typeIdRoot = person.getTypeIdentificationId() != null ? tree.getById(person.getTypeIdentificationId()) : null;
        String typeIdCode = typeIdRoot != null ? typeIdRoot.getShortName() : "CC";

        return new CompanyEmployeeResponse(
                user.getId(),
                user.getUserName(),
                person.getId(),
                person.getName(),
                person.getLastName(),
                person.getNumberid(),
                person.getEmail(),
                person.getPhoneNumber(),
                roleRoot.getId(),
                roleCode,
                roleName,
                category,
                request.outletId(),
                outletName,
                activeStatusId,
                statusName,
                typeIdCode
        );
    }
}

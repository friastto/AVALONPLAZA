package org.frias.avalon.domain.company.application.usecase.assign;

import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.core.validation.PassSecure;
import org.frias.avalon.domain.company.application.dto.request.AssignCompanyManagerRequest;
import org.frias.avalon.domain.company.application.dto.response.CompanyManagerResponse;
import org.frias.avalon.domain.company.domain.model.CompanyDomain;
import org.frias.avalon.domain.company.domain.port.CompanyRepositoryPort;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.person.domain.model.PersonDomain;
import org.frias.avalon.domain.person.domain.port.PersonRepositoryPort;
import org.frias.avalon.domain.user.domain.model.RoleAssignmentDomain;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.frias.avalon.domain.user.domain.port.RoleAssignmentRepositoryPort;
import org.frias.avalon.domain.user.domain.port.UserAvalonRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AssignCompanyManagerUseCaseImpl implements AssignCompanyManagerUseCase {

    private final CompanyRepositoryPort companyRepository;
    private final PersonRepositoryPort personRepository;
    private final UserAvalonRepositoryPort userRepository;
    private final RoleAssignmentRepositoryPort roleAssignmentRepository;
    private final MasterTreeProvider masterTreeProvider;

    public AssignCompanyManagerUseCaseImpl(
            CompanyRepositoryPort companyRepository,
            PersonRepositoryPort personRepository,
            UserAvalonRepositoryPort userRepository,
            RoleAssignmentRepositoryPort roleAssignmentRepository,
            MasterTreeProvider masterTreeProvider
    ) {
        this.companyRepository = companyRepository;
        this.personRepository = personRepository;
        this.userRepository = userRepository;
        this.roleAssignmentRepository = roleAssignmentRepository;
        this.masterTreeProvider = masterTreeProvider;
    }

    @Override
    @Transactional
    public CompanyManagerResponse execute(Long companyId, AssignCompanyManagerRequest request) {
        MasterTree tree = masterTreeProvider.getTree();
        MasterRoot gergenRole = tree.getByCode("GERGEN");
        if (gergenRole == null) {
            throw new ResourceNotFoundException("Rol GERGEN no configurado en MasterTree");
        }
        Long gergenRoleId = gergenRole.getId();

        MasterRoot activeStatus = tree.getByCode("ACT");
        Long activeStatusId = activeStatus != null ? activeStatus.getId() : 1L;

        MasterRoot inactiveStatus = tree.getByCode("INACT");
        Long inactiveStatusId = inactiveStatus != null ? inactiveStatus.getId() : 4L;

        // 1. Validar que la compania exista
        CompanyDomain company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Compania no encontrada con id: " + companyId));

        // 2. Resolver o Crear la Persona
        PersonDomain person;
        if (request.personId() != null) {
            person = personRepository.findById(request.personId())
                    .orElseThrow(() -> new ResourceNotFoundException("Persona no encontrada con id: " + request.personId()));
        } else {
            Optional<PersonDomain> existingPerson = personRepository.findByNumberid(request.identificationNumber());
            if (existingPerson.isPresent()) {
                person = existingPerson.get();
            } else {
                Long phoneNum = null;
                if (request.phone() != null && !request.phone().isBlank()) {
                    try {
                        phoneNum = Long.parseLong(request.phone().replaceAll("[^0-9]", ""));
                    } catch (NumberFormatException ignored) {}
                }

                PersonDomain newPerson = PersonDomain.createBasic(
                        request.identificationTypeId(),
                        request.identificationNumber(),
                        request.firstName(),
                        request.firstLastName(),
                        request.address(),
                        request.sexId() != null ? request.sexId() : 1L,
                        phoneNum,
                        request.email(),
                        activeStatusId
                );
                person = personRepository.save(newPerson);
            }
        }

        // 3. Resolver o Crear el Usuario (UserAvalon)
        UserAvalonDomain user;
        if (request.userId() != null) {
            user = userRepository.findById(request.userId())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + request.userId()));
        } else {
            Optional<UserAvalonDomain> existingUser = userRepository.findByPersonNumberid(person.getNumberid());
            if (existingUser.isPresent()) {
                user = existingUser.get();
            } else {
                String username = (request.username() != null && !request.username().isBlank())
                        ? request.username()
                        : request.identificationNumber();

                String rawPassword = (request.password() != null && !request.password().isBlank())
                        ? request.password()
                        : request.identificationNumber();

                String salt = PassSecure.generateSalt();
                String hashedPassword = PassSecure.hashPassword(rawPassword, salt);

                UserAvalonDomain newUser = UserAvalonDomain.createWithPerson(
                        username,
                        salt,
                        hashedPassword,
                        activeStatusId,
                        person.getId()
                );
                user = userRepository.save(newUser);
            }
        }

        // 4. Validar y Desactivar gerente previo si existe en esta compania
        Optional<RoleAssignmentDomain> currentManagerOpt = roleAssignmentRepository.findByCompanyIdAndRoleId(companyId, gergenRoleId);
        if (currentManagerOpt.isPresent()) {
            RoleAssignmentDomain currentManager = currentManagerOpt.get();
            if (currentManager.getUserId().equals(user.getId()) && currentManager.getStatus().equals(activeStatusId)) {
                // Ya es el gerente activo
                return buildResponse(currentManager, companyId, user, person, gergenRoleId);
            }
            // Desactivar rol previo
            currentManager.changeStatus(inactiveStatusId);
            roleAssignmentRepository.update(currentManager);
        }

        // 5. Crear la nueva asignacion de rol a nivel de empresa (outletId = null)
        RoleAssignmentDomain newAssignment = RoleAssignmentDomain.createCompanyRole(
                user.getId(),
                gergenRoleId,
                companyId,
                activeStatusId
        );
        RoleAssignmentDomain savedAssignment = roleAssignmentRepository.create(newAssignment);

        return buildResponse(savedAssignment, companyId, user, person, gergenRoleId);
    }

    private CompanyManagerResponse buildResponse(
            RoleAssignmentDomain assignment,
            Long companyId,
            UserAvalonDomain user,
            PersonDomain person,
            Long gergenRoleId
    ) {
        String fullName = (person.getName() + " " + (person.getLastName() != null ? person.getLastName() : "")).trim();
        String phoneStr = person.getPhoneNumber() != null ? String.valueOf(person.getPhoneNumber()) : "";
        return new CompanyManagerResponse(
                assignment.getId(),
                companyId,
                user.getId(),
                person.getId(),
                person.getNumberid(),
                fullName,
                person.getEmail(),
                phoneStr,
                user.getUserName(),
                gergenRoleId,
                "GERGEN",
                assignment.getStatus(),
                LocalDateTime.now()
        );
    }
}

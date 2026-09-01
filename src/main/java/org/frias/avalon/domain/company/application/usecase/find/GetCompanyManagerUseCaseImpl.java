package org.frias.avalon.domain.company.application.usecase.find;

import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.domain.company.application.dto.response.CompanyManagerResponse;
import org.frias.avalon.domain.person.domain.model.PersonDomain;
import org.frias.avalon.domain.person.domain.port.PersonRepositoryPort;
import org.frias.avalon.domain.user.domain.model.RoleAssignmentDomain;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.frias.avalon.domain.user.domain.port.RoleAssignmentRepositoryPort;
import org.frias.avalon.domain.user.domain.port.UserAvalonRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class GetCompanyManagerUseCaseImpl implements GetCompanyManagerUseCase {

    private static final Long GERGEN_ROLE_ID = 87L;
    private static final Long STATUS_ACTIVE_ID = 1L;

    private final RoleAssignmentRepositoryPort roleAssignmentRepository;
    private final UserAvalonRepositoryPort userRepository;
    private final PersonRepositoryPort personRepository;

    public GetCompanyManagerUseCaseImpl(
            RoleAssignmentRepositoryPort roleAssignmentRepository,
            UserAvalonRepositoryPort userRepository,
            PersonRepositoryPort personRepository
    ) {
        this.roleAssignmentRepository = roleAssignmentRepository;
        this.userRepository = userRepository;
        this.personRepository = personRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyManagerResponse execute(Long companyId) {
        RoleAssignmentDomain assignment = roleAssignmentRepository.findByCompanyIdAndRoleId(companyId, GERGEN_ROLE_ID)
                .filter(a -> STATUS_ACTIVE_ID.equals(a.getStatus()))
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro un gerente activo para la compania con id: " + companyId));

        UserAvalonDomain user = userRepository.findById(assignment.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + assignment.getUserId()));

        PersonDomain person = personRepository.findById(user.getPersonId())
                .orElseThrow(() -> new ResourceNotFoundException("Persona no encontrada con id: " + user.getPersonId()));

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
                GERGEN_ROLE_ID,
                "GERGEN",
                assignment.getStatus(),
                LocalDateTime.now()
        );
    }
}

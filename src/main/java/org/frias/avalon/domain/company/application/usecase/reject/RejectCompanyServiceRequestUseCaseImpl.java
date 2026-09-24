package org.frias.avalon.domain.company.application.usecase.reject;

import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.core.notification.EmailServicePort;
import org.frias.avalon.domain.company.application.dto.request.RejectCompanyServiceRequestDto;
import org.frias.avalon.domain.company.domain.model.CompanyDomain;
import org.frias.avalon.domain.company.domain.port.CompanyRepositoryPort;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.outlet.domain.model.OutletDomain;
import org.frias.avalon.domain.outlet.domain.port.OutletRepositoryPort;
import org.frias.avalon.domain.person.domain.model.PersonDomain;
import org.frias.avalon.domain.person.domain.port.PersonRepositoryPort;
import org.frias.avalon.domain.user.domain.model.RoleAssignmentDomain;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.frias.avalon.domain.user.domain.port.RoleAssignmentRepositoryPort;
import org.frias.avalon.domain.user.domain.port.UserAvalonRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class RejectCompanyServiceRequestUseCaseImpl implements RejectCompanyServiceRequestUseCase {

    private final CompanyRepositoryPort companyPort;
    private final OutletRepositoryPort outletPort;
    private final RoleAssignmentRepositoryPort roleAssignmentPort;
    private final UserAvalonRepositoryPort userPort;
    private final PersonRepositoryPort personPort;
    private final MasterTreeProvider masterTreeProvider;
    private final EmailServicePort emailServicePort;

    public RejectCompanyServiceRequestUseCaseImpl(
            CompanyRepositoryPort companyPort,
            OutletRepositoryPort outletPort,
            RoleAssignmentRepositoryPort roleAssignmentPort,
            UserAvalonRepositoryPort userPort,
            PersonRepositoryPort personPort,
            MasterTreeProvider masterTreeProvider,
            EmailServicePort emailServicePort
    ) {
        this.companyPort = companyPort;
        this.outletPort = outletPort;
        this.roleAssignmentPort = roleAssignmentPort;
        this.userPort = userPort;
        this.personPort = personPort;
        this.masterTreeProvider = masterTreeProvider;
        this.emailServicePort = emailServicePort;
    }

    @Override
    @Transactional
    public void execute(Long companyId, RejectCompanyServiceRequestDto request) {
        CompanyDomain company = companyPort.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada con id: " + companyId));

        MasterTree tree = masterTreeProvider.getTree();
        MasterRoot recNode = tree.getByCode("REC");
        Long rejectedStatusId = (recNode != null) ? recNode.getId() : tree.getByCodeOrThrow("INA").getId();
        Long inactiveStatusId = tree.getByCodeOrThrow("INA").getId();

        // 1. Actualizar estado de la empresa a rechazado
        CompanyDomain updatedCompany = new CompanyDomain(
                company.id(),
                company.nit(),
                company.name(),
                company.email(),
                rejectedStatusId,
                company.defaultCashThresholdAmount(),
                company.createdAt(),
                company.updatedAt()
        );
        companyPort.save(updatedCompany);

        // 2. Inactivar tiendas vinculadas a la solicitud
        List<OutletDomain> outlets = outletPort.findByCompanyId(companyId);
        for (OutletDomain outlet : outlets) {
            outletPort.update(outlet.withStatus(inactiveStatusId));
        }

        // 3. Inactivar postulacion de rol GERGEN y notificar al solicitante
        Long gergenRoleId = tree.getByCodeOrThrow("GERGEN").getId();
        Optional<RoleAssignmentDomain> assignmentOpt = roleAssignmentPort.findByCompanyIdAndRoleId(companyId, gergenRoleId);

        if (assignmentOpt.isPresent()) {
            RoleAssignmentDomain assignment = assignmentOpt.get();
            assignment.changeStatus(inactiveStatusId);
            roleAssignmentPort.update(assignment);

            Optional<UserAvalonDomain> userOpt = userPort.findById(assignment.getUserId());
            if (userOpt.isPresent()) {
                Optional<PersonDomain> personOpt = personPort.findById(userOpt.get().getPersonId());
                if (personOpt.isPresent()) {
                    PersonDomain person = personOpt.get();
                    String applicantName = (person.getName() + " " + (person.getLastName() != null ? person.getLastName() : "")).trim();
                    String recipientEmail = (person.getEmail() != null && !person.getEmail().isBlank())
                            ? person.getEmail()
                            : company.email();

                    if (recipientEmail != null && !recipientEmail.isBlank()) {
                        emailServicePort.sendCompanyRejectionEmail(
                                recipientEmail,
                                applicantName,
                                company.name(),
                                request.reasonCategory(),
                                request.explanationMessage()
                        );
                    }
                }
            }
        }
    }
}

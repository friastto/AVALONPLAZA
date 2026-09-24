package org.frias.avalon.domain.company.application.usecase.approve;

import org.frias.avalon.core.tenant.port.TenantSchemaMigrationPort;
import org.frias.avalon.domain.company.application.dto.response.CompanyResponse;
import org.frias.avalon.domain.company.domain.model.CompanyDomain;
import org.frias.avalon.domain.company.domain.port.CompanyRepositoryPort;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.outlet.domain.model.OutletDomain;
import org.frias.avalon.domain.outlet.domain.port.OutletRepositoryPort;
import org.frias.avalon.domain.user.domain.model.RoleAssignmentDomain;
import org.frias.avalon.domain.user.domain.port.RoleAssignmentRepositoryPort;
import org.frias.avalon.core.notification.EmailServicePort;
import org.frias.avalon.domain.person.domain.port.PersonRepositoryPort;
import org.frias.avalon.domain.user.domain.port.UserAvalonRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of ApproveCompanyUseCase.
 * Updates company status to ACT (Approved), provisions PostgreSQL tenant schema,
 * activates initial outlets, activates GERGEN role for applicant manager,
 * and notifies the applicant via email.
 */
@Service
public class ApproveCompanyUseCaseImpl implements ApproveCompanyUseCase {

    private final CompanyRepositoryPort companyPort;
    private final TenantSchemaMigrationPort tenantSchemaMigrationPort;
    private final MasterTreeProvider masterTreeProvider;
    private final OutletRepositoryPort outletPort;
    private final RoleAssignmentRepositoryPort roleAssignmentRepository;
    private final UserAvalonRepositoryPort userPort;
    private final PersonRepositoryPort personPort;
    private final EmailServicePort emailServicePort;

    public ApproveCompanyUseCaseImpl(
            CompanyRepositoryPort companyPort,
            TenantSchemaMigrationPort tenantSchemaMigrationPort,
            MasterTreeProvider masterTreeProvider,
            OutletRepositoryPort outletPort,
            RoleAssignmentRepositoryPort roleAssignmentRepository,
            UserAvalonRepositoryPort userPort,
            PersonRepositoryPort personPort,
            EmailServicePort emailServicePort
    ) {
        this.companyPort = companyPort;
        this.tenantSchemaMigrationPort = tenantSchemaMigrationPort;
        this.masterTreeProvider = masterTreeProvider;
        this.outletPort = outletPort;
        this.roleAssignmentRepository = roleAssignmentRepository;
        this.userPort = userPort;
        this.personPort = personPort;
        this.emailServicePort = emailServicePort;
    }

    @Transactional
    @Override
    public CompanyResponse execute(Long companyId) {
        CompanyDomain company = companyPort.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company with ID " + companyId + " not found"));

        MasterTree tree = masterTreeProvider.getTree();
        Long approvedStatusId = tree.getByCodeOrThrow("ACT").getId();

        CompanyDomain approvedDomain = new CompanyDomain(
                company.id(),
                company.nit(),
                company.name(),
                company.email(),
                approvedStatusId,
                company.defaultCashThresholdAmount(),
                company.createdAt(),
                company.updatedAt()
        );

        CompanyDomain saved = companyPort.save(approvedDomain);
        tenantSchemaMigrationPort.migrateTenantSchema("company_" + companyId);

        // 1. Activar tiendas iniciales asociadas a la empresa
        List<OutletDomain> outlets = outletPort.findByCompanyId(companyId);
        for (OutletDomain outlet : outlets) {
            if (!approvedStatusId.equals(outlet.getStatusId())) {
                OutletDomain activeOutlet = outlet.withStatus(approvedStatusId);
                outletPort.update(activeOutlet);
            }
        }

        // 2. Activar asignacion de rol GERGEN para el postulante de la empresa
        MasterRoot gergenRole = tree.getByCode("GERGEN");
        if (gergenRole != null) {
            Optional<RoleAssignmentDomain> managerRoleOpt = roleAssignmentRepository.findByCompanyIdAndRoleId(companyId, gergenRole.getId());
            if (managerRoleOpt.isPresent()) {
                RoleAssignmentDomain managerRole = managerRoleOpt.get();
                managerRole.changeStatus(approvedStatusId);
                roleAssignmentRepository.update(managerRole);

                if (userPort != null && personPort != null && emailServicePort != null) {
                    userPort.findById(managerRole.getUserId()).ifPresent(user -> {
                        personPort.findById(user.getPersonId()).ifPresent(person -> {
                            String applicantName = (person.getName() + " " + (person.getLastName() != null ? person.getLastName() : "")).trim();
                            String recipientEmail = (person.getEmail() != null && !person.getEmail().isBlank()) ? person.getEmail() : saved.email();
                            if (recipientEmail != null && !recipientEmail.isBlank()) {
                                emailServicePort.sendCompanyApprovalEmail(recipientEmail, applicantName, saved.name());
                            }
                        });
                    });
                }
            }
        }

        return CompanyResponse.from(saved, tree);
    }
}


package org.frias.avalon.domain.company.application.usecase.approve;

import org.frias.avalon.core.tenant.FlywayMultiTenantService;
import org.frias.avalon.domain.company.application.dto.response.CompanyResponse;
import org.frias.avalon.domain.company.domain.model.CompanyDomain;
import org.frias.avalon.domain.company.domain.port.CompanyRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of ApproveCompanyUseCase.
 * Updates company status to 1L (Approved) and provisions PostgreSQL tenant schema.
 */
@Service
public class ApproveCompanyUseCaseImpl implements ApproveCompanyUseCase {

    private final CompanyRepositoryPort companyPort;
    private final FlywayMultiTenantService flywayMultiTenantService;

    public ApproveCompanyUseCaseImpl(CompanyRepositoryPort companyPort, FlywayMultiTenantService flywayMultiTenantService) {
        this.companyPort = companyPort;
        this.flywayMultiTenantService = flywayMultiTenantService;
    }

    @Transactional
    @Override
    public CompanyResponse execute(Long companyId) {
        CompanyDomain company = companyPort.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company with ID " + companyId + " not found"));

        CompanyDomain approvedDomain = new CompanyDomain(
                company.id(),
                company.nit(),
                company.name(),
                company.email(),
                1L, // statusId: 1L (Approved)
                company.defaultCashThresholdAmount(),
                company.createdAt(),
                company.updatedAt()
        );

        CompanyDomain saved = companyPort.save(approvedDomain);
        flywayMultiTenantService.migrateTenantSchema("company_" + companyId);

        return new CompanyResponse(
                saved.id(),
                saved.nit(),
                saved.name(),
                saved.email(),
                saved.statusId(),
                saved.defaultCashThresholdAmount(),
                saved.createdAt(),
                saved.updatedAt()
        );
    }
}

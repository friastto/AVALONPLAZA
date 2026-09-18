package org.frias.avalon.domain.company.application.usecase.approve;

import org.frias.avalon.core.tenant.port.TenantSchemaMigrationPort;
import org.frias.avalon.domain.company.application.dto.response.CompanyResponse;
import org.frias.avalon.domain.company.domain.model.CompanyDomain;
import org.frias.avalon.domain.company.domain.port.CompanyRepositoryPort;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of ApproveCompanyUseCase.
 * Updates company status to 1L (Approved) and provisions PostgreSQL tenant schema.
 */
@Service
public class ApproveCompanyUseCaseImpl implements ApproveCompanyUseCase {

    private final CompanyRepositoryPort companyPort;
    private final TenantSchemaMigrationPort tenantSchemaMigrationPort;
    private final MasterTreeProvider masterTreeProvider;

    public ApproveCompanyUseCaseImpl(
            CompanyRepositoryPort companyPort,
            TenantSchemaMigrationPort tenantSchemaMigrationPort,
            MasterTreeProvider masterTreeProvider
    ) {
        this.companyPort = companyPort;
        this.tenantSchemaMigrationPort = tenantSchemaMigrationPort;
        this.masterTreeProvider = masterTreeProvider;
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

        return CompanyResponse.from(saved, tree);
    }
}

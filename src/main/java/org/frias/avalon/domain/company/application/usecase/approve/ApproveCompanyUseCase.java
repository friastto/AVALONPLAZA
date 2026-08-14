package org.frias.avalon.domain.company.application.usecase.approve;

import org.frias.avalon.domain.company.application.dto.response.CompanyResponse;

/**
 * Use case port for approving a company request and provisioning its multi-tenant schema.
 */
public interface ApproveCompanyUseCase {

    /**
     * Executes the use case to approve a company by ID.
     *
     * @param companyId company ID to approve
     * @return CompanyResponse DTO of approved company
     */
    CompanyResponse execute(Long companyId);
}

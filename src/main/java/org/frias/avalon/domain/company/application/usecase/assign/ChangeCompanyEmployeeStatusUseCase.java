package org.frias.avalon.domain.company.application.usecase.assign;

import org.frias.avalon.domain.company.application.dto.request.UpdateCompanyEmployeeStatusRequest;
import org.frias.avalon.domain.company.application.dto.response.CompanyEmployeeResponse;

/**
 * Use case to activate, deactivate or suspend a company or store employee's role assignment.
 * Does not alter the global consumer user account.
 */
public interface ChangeCompanyEmployeeStatusUseCase {
    CompanyEmployeeResponse execute(Long companyId, Long userId, UpdateCompanyEmployeeStatusRequest request);
}

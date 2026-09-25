package org.frias.avalon.domain.company.application.usecase.assign;

import org.frias.avalon.domain.company.application.dto.request.TransferCompanyEmployeeRequest;
import org.frias.avalon.domain.company.application.dto.response.CompanyEmployeeResponse;

/**
 * Use case to transfer an employee from one outlet to another within the same company.
 */
public interface TransferCompanyEmployeeUseCase {
    CompanyEmployeeResponse execute(Long companyId, Long userId, TransferCompanyEmployeeRequest request);
}

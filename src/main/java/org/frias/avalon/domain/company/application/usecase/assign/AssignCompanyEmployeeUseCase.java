package org.frias.avalon.domain.company.application.usecase.assign;

import org.frias.avalon.domain.company.application.dto.request.AssignCompanyEmployeeRequest;
import org.frias.avalon.domain.company.application.dto.response.CompanyEmployeeResponse;

public interface AssignCompanyEmployeeUseCase {
    CompanyEmployeeResponse execute(Long companyId, AssignCompanyEmployeeRequest request);
}

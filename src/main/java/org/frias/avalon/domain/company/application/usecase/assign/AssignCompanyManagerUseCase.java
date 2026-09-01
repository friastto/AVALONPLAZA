package org.frias.avalon.domain.company.application.usecase.assign;

import org.frias.avalon.domain.company.application.dto.request.AssignCompanyManagerRequest;
import org.frias.avalon.domain.company.application.dto.response.CompanyManagerResponse;

public interface AssignCompanyManagerUseCase {
    CompanyManagerResponse execute(Long companyId, AssignCompanyManagerRequest request);
}

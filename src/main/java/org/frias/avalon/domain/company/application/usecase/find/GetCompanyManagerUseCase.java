package org.frias.avalon.domain.company.application.usecase.find;

import org.frias.avalon.domain.company.application.dto.response.CompanyManagerResponse;

public interface GetCompanyManagerUseCase {
    CompanyManagerResponse execute(Long companyId);
}

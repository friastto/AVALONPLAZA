package org.frias.avalon.domain.company.application.usecase.find;

import org.frias.avalon.domain.company.application.dto.response.CompanyServiceRequestDetailResponse;

public interface FindCompanyServiceRequestDetailUseCase {
    CompanyServiceRequestDetailResponse execute(Long companyId);
}

package org.frias.avalon.domain.company.application.usecase.create;

import org.frias.avalon.domain.company.application.dto.request.CreateCompanyServiceRequestDto;
import org.frias.avalon.domain.company.application.dto.response.CompanyResponse;

public interface CreateCompanyServiceRequestUseCase {
    CompanyResponse execute(CreateCompanyServiceRequestDto request);
}

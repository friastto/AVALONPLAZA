package org.frias.avalon.domain.company.application.usecase.create;

import org.frias.avalon.domain.company.application.dto.request.CreateCompanyRequest;
import org.frias.avalon.domain.company.application.dto.response.CompanyResponse;

/**
 * Use case port for creating a new Company.
 */
public interface CreateCompanyUseCase {

    CompanyResponse execute(CreateCompanyRequest request);
}

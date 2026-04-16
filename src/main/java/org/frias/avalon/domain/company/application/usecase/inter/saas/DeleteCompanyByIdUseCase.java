package org.frias.avalon.domain.company.application.usecase.inter.saas;

import org.frias.avalon.domain.company.application.dtos.response.CompanyWhithMainOutletResponseDto;

public interface DeleteCompanyByIdUseCase {

    CompanyWhithMainOutletResponseDto execute(Long id);
}

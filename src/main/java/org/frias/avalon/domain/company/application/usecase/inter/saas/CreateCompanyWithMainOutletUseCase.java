package org.frias.avalon.domain.company.application.usecase.inter.saas;

import org.frias.avalon.domain.company.application.dtos.request.CompanyWithOutletDto;
import org.frias.avalon.domain.company.application.dtos.response.CompanyWhithMainOutletResponseDto;

public interface CreateCompanyWithMainOutletUseCase {

    CompanyWhithMainOutletResponseDto execute(CompanyWithOutletDto request);

}

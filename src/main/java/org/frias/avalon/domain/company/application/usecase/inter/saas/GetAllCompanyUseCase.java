package org.frias.avalon.domain.company.application.usecase.inter.saas;

import org.frias.avalon.domain.company.application.dtos.response.CompanyWhithMainOutletResponseDto;

import java.util.List;

public interface GetAllCompanyUseCase {

    List<CompanyWhithMainOutletResponseDto> execute();

}

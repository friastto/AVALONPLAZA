package org.frias.avalon.domain.company.application.usecase.find;

import org.frias.avalon.domain.outlet.application.dto.response.OutletResponseDto;

import java.util.List;

/**
 * Use case port for retrieving all outlets linked to a company.
 */
public interface FindOutletsByCompanyUseCase {

    /**
     * Executes the use case to find outlets linked to company.
     *
     * @param companyId company ID
     * @return list of OutletResponseDto
     */
    List<OutletResponseDto> execute(Long companyId);
}

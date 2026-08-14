package org.frias.avalon.domain.company.application.usecase.find;

import org.frias.avalon.domain.company.application.dto.response.CompanyResponse;

import java.util.List;

/**
 * Use case port for retrieving companies pending approval (status RVW / 1L).
 */
public interface FindPendingCompaniesUseCase {

    /**
     * Executes the use case to retrieve pending companies.
     *
     * @return list of pending CompanyResponse DTOs
     */
    List<CompanyResponse> execute();
}

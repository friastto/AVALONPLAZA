package org.frias.avalon.domain.company.application.usecase.find;

import org.frias.avalon.domain.company.application.dto.response.CompanyResponse;

import java.util.Optional;

/**
 * Use case port for retrieving a company by its ID.
 */
public interface FindCompanyByIdUseCase {

    /**
     * Executes the use case to find a company by ID.
     *
     * @param id company ID
     * @return Optional containing CompanyResponse if found
     */
    Optional<CompanyResponse> execute(Long id);
}

package org.frias.avalon.domain.company.application.usecase.find;

import org.frias.avalon.domain.company.application.dto.response.CompanyResponse;

import java.util.List;

/**
 * Use case port for finding all companies.
 */
public interface FindAllCompaniesUseCase {

    List<CompanyResponse> execute();
}

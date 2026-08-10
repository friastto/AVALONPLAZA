package org.frias.avalon.domain.company.application.usecase.find;

import org.frias.avalon.domain.company.application.dto.response.CompanyResponse;
import org.frias.avalon.domain.company.domain.port.CompanyRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of FindAllCompaniesUseCase.
 * Returns all registered companies in the platform.
 */
@Service
public class FindAllCompaniesUseCaseImpl implements FindAllCompaniesUseCase {

    private final CompanyRepositoryPort companyPort;

    public FindAllCompaniesUseCaseImpl(CompanyRepositoryPort companyPort) {
        this.companyPort = companyPort;
    }

    @Transactional(readOnly = true)
    @Override
    public List<CompanyResponse> execute() {
        return companyPort.findAll().stream()
                .map(domain -> new CompanyResponse(
                        domain.id(),
                        domain.nit(),
                        domain.name(),
                        domain.email(),
                        domain.statusId(),
                        domain.defaultCashThresholdAmount(),
                        domain.createdAt(),
                        domain.updatedAt()
                ))
                .toList();
    }
}

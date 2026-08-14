package org.frias.avalon.domain.company.application.usecase.find;

import org.frias.avalon.domain.company.application.dto.response.CompanyResponse;
import org.frias.avalon.domain.company.domain.port.CompanyRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of FindPendingCompaniesUseCase.
 * Fetches companies with statusId 1L (RVW / Pending Approval) from domain repository.
 */
@Service
public class FindPendingCompaniesUseCaseImpl implements FindPendingCompaniesUseCase {

    private final CompanyRepositoryPort companyPort;

    public FindPendingCompaniesUseCaseImpl(CompanyRepositoryPort companyPort) {
        this.companyPort = companyPort;
    }

    @Transactional(readOnly = true)
    @Override
    public List<CompanyResponse> execute() {
        return companyPort.findByStatusId(1L).stream()
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

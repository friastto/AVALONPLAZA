package org.frias.avalon.domain.company.application.usecase.find;

import org.frias.avalon.domain.company.application.dto.response.CompanyResponse;
import org.frias.avalon.domain.company.domain.port.CompanyRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Implementation of FindCompanyByIdUseCase.
 */
@Service
public class FindCompanyByIdUseCaseImpl implements FindCompanyByIdUseCase {

    private final CompanyRepositoryPort companyPort;

    public FindCompanyByIdUseCaseImpl(CompanyRepositoryPort companyPort) {
        this.companyPort = companyPort;
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<CompanyResponse> execute(Long id) {
        return companyPort.findById(id).map(domain -> new CompanyResponse(
                domain.id(),
                domain.nit(),
                domain.name(),
                domain.email(),
                domain.statusId(),
                domain.defaultCashThresholdAmount(),
                domain.createdAt(),
                domain.updatedAt()
        ));
    }
}

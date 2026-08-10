package org.frias.avalon.domain.company.application.usecase.create;

import org.frias.avalon.domain.company.application.dto.request.CreateCompanyRequest;
import org.frias.avalon.domain.company.application.dto.response.CompanyResponse;
import org.frias.avalon.domain.company.domain.model.CompanyDomain;
import org.frias.avalon.domain.company.domain.port.CompanyRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of CreateCompanyUseCase.
 * Validates unique NIT before persisting a new Company.
 */
@Service
public class CreateCompanyUseCaseImpl implements CreateCompanyUseCase {

    private final CompanyRepositoryPort companyPort;

    public CreateCompanyUseCaseImpl(CompanyRepositoryPort companyPort) {
        this.companyPort = companyPort;
    }

    @Transactional
    @Override
    public CompanyResponse execute(CreateCompanyRequest request) {

        // Business invariant: NIT must be unique across companies
        companyPort.findByNit(request.nit()).ifPresent(existing -> {
            throw new IllegalStateException("Company with NIT " + request.nit() + " already exists");
        });

        CompanyDomain toSave = new CompanyDomain(
                null,
                request.nit(),
                request.name(),
                request.email(),
                null,   // statusId assigned after creation if needed
                request.defaultCashThresholdAmount(),
                null,
                null
        );

        CompanyDomain saved = companyPort.save(toSave);

        return new CompanyResponse(
                saved.id(),
                saved.nit(),
                saved.name(),
                saved.email(),
                saved.statusId(),
                saved.defaultCashThresholdAmount(),
                saved.createdAt(),
                saved.updatedAt()
        );
    }
}

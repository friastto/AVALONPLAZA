package org.frias.avalon.domain.subscription.application.usecase;

import org.frias.avalon.domain.company.domain.port.CompanyRepositoryPort;
import org.frias.avalon.domain.subscription.application.dto.request.AcceptPoliciesRequestDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of AcceptCompanyPoliciesUseCase.
 */
@Service
public class AcceptCompanyPoliciesUseCaseImpl implements AcceptCompanyPoliciesUseCase {

    private final CompanyRepositoryPort companyRepositoryPort;

    public AcceptCompanyPoliciesUseCaseImpl(CompanyRepositoryPort companyRepositoryPort) {
        this.companyRepositoryPort = companyRepositoryPort;
    }

    @Override
    @Transactional
    public void execute(Long companyId, AcceptPoliciesRequestDto request) {
        if (companyId == null) {
            throw new IllegalArgumentException("Company ID is required");
        }
        String version = (request != null && request.policiesVersion() != null)
                ? request.policiesVersion()
                : "v1.0";
        companyRepositoryPort.acceptPolicies(companyId, version);
    }
}

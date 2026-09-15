package org.frias.avalon.domain.company.application.usecase.find;

import org.frias.avalon.core.permissions.CurrentUserProviderPort;
import org.frias.avalon.domain.company.application.dto.response.CompanyResponse;
import org.frias.avalon.domain.company.domain.port.CompanyRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of FindAllCompaniesUseCase.
 * Returns all registered companies in the platform, filtered by tenant for corporate managers.
 */
@Service
public class FindAllCompaniesUseCaseImpl implements FindAllCompaniesUseCase {

    private final CompanyRepositoryPort companyPort;
    private final CurrentUserProviderPort currentUserProvider;

    public FindAllCompaniesUseCaseImpl(CompanyRepositoryPort companyPort, CurrentUserProviderPort currentUserProvider) {
        this.companyPort = companyPort;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional(readOnly = true)
    @Override
    public List<CompanyResponse> execute() {
        boolean isSuperAdmin = currentUserProvider.hasRole("ROLE_ADMINTI") || currentUserProvider.hasRole("ROLE_ADMINSYS") || currentUserProvider.hasRole("ROLE_ADMIN");
        Long currentTenantId = currentUserProvider.getCurrentTenantId();

        return companyPort.findAll().stream()
                .filter(domain -> isSuperAdmin || (currentTenantId != null && currentTenantId.equals(domain.id())))
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

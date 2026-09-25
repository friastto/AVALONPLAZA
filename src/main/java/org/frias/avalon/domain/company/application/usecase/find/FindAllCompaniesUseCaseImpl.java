package org.frias.avalon.domain.company.application.usecase.find;

import org.frias.avalon.core.permissions.CurrentUserProviderPort;
import org.frias.avalon.domain.company.application.dto.response.CompanyResponse;
import org.frias.avalon.domain.company.domain.port.CompanyRepositoryPort;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of FindAllCompaniesUseCase.
 * Returns active registered companies in the platform, filtered by tenant for corporate managers.
 */
@Service
public class FindAllCompaniesUseCaseImpl implements FindAllCompaniesUseCase {

    private final CompanyRepositoryPort companyPort;
    private final CurrentUserProviderPort currentUserProvider;
    private final MasterTreeProvider masterTreeProvider;

    public FindAllCompaniesUseCaseImpl(
            CompanyRepositoryPort companyPort,
            CurrentUserProviderPort currentUserProvider,
            MasterTreeProvider masterTreeProvider) {
        this.companyPort = companyPort;
        this.currentUserProvider = currentUserProvider;
        this.masterTreeProvider = masterTreeProvider;
    }

    @Transactional(readOnly = true)
    @Override
    public List<CompanyResponse> execute() {
        boolean isSuperAdmin = currentUserProvider.hasRole("ROLE_ADMINTI") || currentUserProvider.hasRole("ROLE_ADMINSYS") || currentUserProvider.hasRole("ROLE_ADMIN");
        Long currentTenantId = currentUserProvider.getCurrentTenantId();
        MasterTree tree = masterTreeProvider.getTree();

        return companyPort.findAll().stream()
                .filter(domain -> {
                    if (domain.statusId() == null) return false;
                    MasterRoot statusNode = tree.getById(domain.statusId());
                    return tree.is(statusNode, "ACT");
                })
                .filter(domain -> isSuperAdmin || (currentTenantId != null && currentTenantId.equals(domain.id())))
                .map(domain -> CompanyResponse.from(domain, tree))
                .toList();
    }
}

package org.frias.avalon.domain.company.application.usecase.find;

import org.frias.avalon.domain.company.application.dto.response.CompanyResponse;
import org.frias.avalon.domain.company.domain.port.CompanyRepositoryPort;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Implementation of FindCompanyByIdUseCase.
 */
@Service
public class FindCompanyByIdUseCaseImpl implements FindCompanyByIdUseCase {

    private final CompanyRepositoryPort companyPort;
    private final MasterTreeProvider masterTreeProvider;

    public FindCompanyByIdUseCaseImpl(CompanyRepositoryPort companyPort, MasterTreeProvider masterTreeProvider) {
        this.companyPort = companyPort;
        this.masterTreeProvider = masterTreeProvider;
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<CompanyResponse> execute(Long id) {
        var tree = masterTreeProvider.getTree();
        return companyPort.findById(id).map(domain -> CompanyResponse.from(domain, tree));
    }
}

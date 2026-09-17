package org.frias.avalon.domain.company.application.usecase.find;

import org.frias.avalon.domain.company.application.dto.response.CompanyResponse;
import org.frias.avalon.domain.company.domain.port.CompanyRepositoryPort;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
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
    private final MasterTreeProvider masterTreeProvider;

    public FindPendingCompaniesUseCaseImpl(
            CompanyRepositoryPort companyPort,
            MasterTreeProvider masterTreeProvider
    ) {
        this.companyPort = companyPort;
        this.masterTreeProvider = masterTreeProvider;
    }

    @Transactional(readOnly = true)
    @Override
    public List<CompanyResponse> execute() {
        MasterTree tree = masterTreeProvider.getTree();
        MasterRoot rvwNode = tree.getByCode("RVW");
        Long pendingStatusId = (rvwNode != null) ? rvwNode.getId() : tree.getByCodeOrThrow("ACT").getId();

        return companyPort.findByStatusId(pendingStatusId).stream()
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

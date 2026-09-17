package org.frias.avalon.domain.company.application.usecase.create;

import org.frias.avalon.domain.company.application.dto.request.CreateCompanyRequest;
import org.frias.avalon.domain.company.application.dto.response.CompanyResponse;
import org.frias.avalon.domain.company.domain.model.CompanyDomain;
import org.frias.avalon.domain.company.domain.port.CompanyRepositoryPort;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of CreateCompanyUseCase.
 * Validates unique NIT before persisting a new Company.
 */
@Service
public class CreateCompanyUseCaseImpl implements CreateCompanyUseCase {

    private final CompanyRepositoryPort companyPort;
    private final MasterTreeProvider masterTreeProvider;

    public CreateCompanyUseCaseImpl(
            CompanyRepositoryPort companyPort,
            MasterTreeProvider masterTreeProvider
    ) {
        this.companyPort = companyPort;
        this.masterTreeProvider = masterTreeProvider;
    }

    @Transactional
    @Override
    public CompanyResponse execute(CreateCompanyRequest request) {

        // Business invariant: NIT must be unique across companies
        companyPort.findByNit(request.nit()).ifPresent(existing -> {
            throw new IllegalStateException("Company with NIT " + request.nit() + " already exists");
        });

        MasterTree tree = masterTreeProvider.getTree();
        MasterRoot rvwNode = tree.getByCode("RVW");
        if (rvwNode == null) {
            rvwNode = tree.getByCode("ACT");
        }
        Long statusId = (rvwNode != null) ? rvwNode.getId() : tree.getByCodeOrThrow("ACT").getId();

        CompanyDomain toSave = new CompanyDomain(
                null,
                request.nit(),
                request.name(),
                request.email(),
                statusId,
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

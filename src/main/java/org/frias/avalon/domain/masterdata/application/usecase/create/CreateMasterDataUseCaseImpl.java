package org.frias.avalon.domain.masterdata.application.usecase.create;

import org.frias.avalon.domain.masterdata.application.dto.request.MasterDataNewDto;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.repository.MasterDataRepositoryPort;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.springframework.stereotype.Service;

@Service
public class CreateMasterDataUseCaseImpl implements CreateMasterDataUseCase {

    private final MasterDataRepositoryPort masterDataRepositoryPort;
    private final MasterTreeProvider masterTreeProvider;

    public CreateMasterDataUseCaseImpl(MasterDataRepositoryPort masterDataRepositoryPort, MasterTreeProvider masterTreeProvider) {
        this.masterDataRepositoryPort = masterDataRepositoryPort;
        this.masterTreeProvider = masterTreeProvider;
    }


    @Override
    public Long execute(MasterDataNewDto request) {

        if (request.shortName() == null || request.shortName().isBlank()) {
            throw new RuntimeException("shortName requerido");
        }
        if (request.fullName() == null || request.fullName().isBlank()) {
            throw new RuntimeException("fullName requerido");
        }

        Long parentId = null;
        if (request.parentShortName() != null && !request.parentShortName().isBlank()) {
            MasterRoot parentNode = masterTreeProvider.getTree() != null
                    ? masterTreeProvider.getTree().getByCode(request.parentShortName().trim().toUpperCase())
                    : null;
            parentId = parentNode != null ? parentNode.getId() : masterDataRepositoryPort.getIdByCode(request.parentShortName().trim().toUpperCase());
        }

        MasterRoot statusNode = masterTreeProvider.getTree() != null
                ? masterTreeProvider.getTree().getByCode("ACT")
                : null;
        Long statusId = statusNode != null ? statusNode.getId() : masterDataRepositoryPort.getIdByCode("ACT");

        MasterRoot domain = MasterRoot.create(request.shortName(), request.fullName(), parentId, statusId);

        MasterRoot saved = masterDataRepositoryPort.save(domain);
        masterTreeProvider.refresh();

        return saved.getId();
    }
}
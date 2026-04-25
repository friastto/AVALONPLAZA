package org.frias.avalon.domain.masterdata.application.usecase.create;

import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.repository.MasterDataRepositoryPort;
import org.frias.avalon.domain.masterdata.application.dto.request.MasterDataNewDto;
import org.springframework.stereotype.Service;

@Service
public class CreateMasterDataUseCaseImpl implements CreateMasterDataUseCase {

    private final MasterDataRepositoryPort masterDataRepositoryPort;

    public CreateMasterDataUseCaseImpl(MasterDataRepositoryPort masterDataRepositoryPort) {
        this.masterDataRepositoryPort = masterDataRepositoryPort;
    }


    @Override
    public Long execute(MasterDataNewDto request) {

        Long parentId = masterDataRepositoryPort.getIdByCode(request.parentShortName());

        Long statusId =  masterDataRepositoryPort.getIdByCode("ACT");

        MasterRoot domain = MasterRoot.create(request.shortName(), request.fullName(), parentId,statusId);

        return masterDataRepositoryPort.save(domain).getId();


    }
}

package org.frias.avalon.domain.masterdata.application.usecase.delete;

import org.frias.avalon.domain.masterdata.application.dto.response.MasterDataResponseDto;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.repository.MasterDataRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeleteMasterDataUseCaseImpl implements DeleteMasterDataUseCase {

    private final MasterDataRepositoryPort masterDataRepositoryPort;

    public DeleteMasterDataUseCaseImpl(MasterDataRepositoryPort masterDataRepositoryPort) {
        this.masterDataRepositoryPort = masterDataRepositoryPort;
    }

    @Override
    @Transactional
    public MasterDataResponseDto execute(Long id) {
        MasterRoot deletedDomainObject = masterDataRepositoryPort.deleteById(id);
        
        // Mapeo manual para evitar modificar el mapper service
        return new MasterDataResponseDto(
                deletedDomainObject.getId(),
                deletedDomainObject.getShortName(),
                deletedDomainObject.getFullName()
        );
    }
}
package org.frias.avalon.domain.masterdata.application.usecase.reparent;

import org.frias.avalon.domain.masterdata.application.dto.response.MasterDataResponseDto;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.repository.MasterDataRepositoryPort;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of ReparentMasterDataUseCase.
 */
@Service
public class ReparentMasterDataUseCaseImpl implements ReparentMasterDataUseCase {

    private final MasterDataRepositoryPort masterDataRepositoryPort;
    private final MasterTreeProvider masterTreeProvider;

    public ReparentMasterDataUseCaseImpl(MasterDataRepositoryPort masterDataRepositoryPort, MasterTreeProvider masterTreeProvider) {
        this.masterDataRepositoryPort = masterDataRepositoryPort;
        this.masterTreeProvider = masterTreeProvider;
    }

    /**
     * Updates the parent ID of a specified MasterData node.
     *
     * @param id the ID of the node to reparent
     * @param newParentId the ID of the new parent node
     * @return MasterDataResponseDto representing the updated node
     */
    @Override
    @Transactional
    public MasterDataResponseDto execute(Long id, Long newParentId) {
        MasterRoot updatedDomainObject = masterDataRepositoryPort.updateParentId(id, newParentId);
        masterTreeProvider.refresh();
        return new MasterDataResponseDto(
                updatedDomainObject.getId(),
                updatedDomainObject.getShortName(),
                updatedDomainObject.getFullName(),
                updatedDomainObject.getParentId(),
                "ACTIVO"
        );
    }
}

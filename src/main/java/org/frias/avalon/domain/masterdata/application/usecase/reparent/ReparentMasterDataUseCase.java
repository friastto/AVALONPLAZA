package org.frias.avalon.domain.masterdata.application.usecase.reparent;

import org.frias.avalon.domain.masterdata.application.dto.response.MasterDataResponseDto;

/**
 * Use case interface for reparenting a master data node.
 */
public interface ReparentMasterDataUseCase {

    /**
     * Updates the parent ID of a specified MasterData node.
     *
     * @param id the ID of the node to reparent
     * @param newParentId the ID of the new parent node
     * @return MasterDataResponseDto representing the updated node
     */
    MasterDataResponseDto execute(Long id, Long newParentId);
}

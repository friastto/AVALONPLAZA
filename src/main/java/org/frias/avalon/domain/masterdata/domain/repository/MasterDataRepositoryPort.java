package org.frias.avalon.domain.masterdata.domain.repository;

import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;

import java.util.List;
import java.util.Optional;

public interface MasterDataRepositoryPort {

    Optional<MasterRoot> findById(Long id);

    Optional<MasterRoot> findByCode(String code);

    Long getIdByCode(String code);

    String getCodeById(Long id);

    MasterRoot save(MasterRoot masterData);

    MasterRoot deleteById(Long id);

    /**
     * Updates the parent ID for a specified MasterData node.
     *
     * @param nodeId the ID of the node to update
     * @param newParentId the new parent ID
     * @return the updated MasterRoot domain model
     */
    MasterRoot updateParentId(Long nodeId, Long newParentId);

    Optional<MasterRoot> findParentByChildrenId(Long chilldrenId);

    Optional<MasterRoot> getActiveStatus();

    List<MasterRoot> findAll();

    List<MasterRoot> saveAll(List<MasterRoot> mdList2);

    List<MasterRoot> findChildrenByParentCode(String parentCode);
}

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

    Optional<MasterRoot> findParentByChildrenId(Long chilldrenId);

    Optional<MasterRoot> getActiveStatus();

    List<MasterRoot> findAll();

    List<MasterRoot> saveAll(List<MasterRoot> mdList2);
}

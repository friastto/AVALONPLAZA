package org.frias.avalon.domain.masterdata.domain.repository;

import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;

import java.util.Optional;

public interface MasterDataRepositoryPort {

        Optional<MasterRoot> findById(Long id);

        Optional<MasterRoot> findByCode(String code);

        Long getIdByCode(String code);

        String getCodeById(Long id);

        MasterRoot save(MasterRoot masterData);

        MasterRoot deleteById(Long id);

        Optional<MasterRoot> findParentByIClilldrenId(Long chilldrenId);
}

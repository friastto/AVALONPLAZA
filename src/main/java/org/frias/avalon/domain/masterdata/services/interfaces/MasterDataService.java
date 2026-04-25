package org.frias.avalon.domain.masterdata.services.interfaces;

import org.frias.avalon.domain.masterdata.application.dto.request.MasterDataNewDto;
import org.frias.avalon.domain.masterdata.infraestructure.persistence.entity.MasterData;

import java.util.List;

public interface MasterDataService {
    MasterData create(MasterDataNewDto request);

    List<MasterData> createAll(List<MasterDataNewDto> request);

    MasterData searchById(Long id);

    MasterData searchByShortName(String shortName);

    MasterData searchByNameShortAndStatusActive(String nameShort);

    MasterData getRootBranch(Long id, String rootShortName);

    boolean isFromHierarchy(Long id, String branchName);

    List<MasterData> getAllSonWithStatusActiveByParentNameShort(String ParentShortName);

    List<MasterData> getAllSonWithStatusActiveByParentId(Long idParent);

    List<MasterData> getAllWithStatusActive();

    MasterData getStatusActive();
}

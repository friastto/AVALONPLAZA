package org.frias.avalon.maestra.services.interfaces;

import org.frias.avalon.maestra.dtos.MasterDataRequestCreateDto;
import org.frias.avalon.maestra.dtos.MasterDataResponseDto;
import org.frias.avalon.maestra.entities.MasterData;

import java.util.List;

public interface MasterDataService {
    List<MasterDataResponseDto> saveAll(List<MasterDataRequestCreateDto> masterDataRequestList);
    MasterDataResponseDto findByNameShortDto(String nameShort);
    MasterData findById(Long id);
    MasterData searchShortName(String shortName);

    MasterData getRootBranch(Long id, String rootShortName);
    boolean isFromHierarchy(Long id, String branchName);
}

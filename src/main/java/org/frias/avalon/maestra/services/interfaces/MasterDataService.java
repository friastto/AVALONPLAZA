package org.frias.avalon.maestra.services.interfaces;

import org.frias.avalon.maestra.dtos.MasterDataRequestCreateDto;
import org.frias.avalon.maestra.dtos.MasterDataResponseDto;

import java.util.List;

public interface MasterDataService {
    List<MasterDataResponseDto> saveAll(List<MasterDataRequestCreateDto> masterDataRequestList);
    MasterDataResponseDto findByNameShort(String nameShort);

}

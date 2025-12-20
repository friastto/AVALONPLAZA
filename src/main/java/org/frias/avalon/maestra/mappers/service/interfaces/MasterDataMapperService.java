package org.frias.avalon.maestra.mappers.service.interfaces;

import org.frias.avalon.maestra.dtos.MasterDataRequestCreateDto;
import org.frias.avalon.maestra.dtos.MasterDataResponseDto;
import org.frias.avalon.maestra.entities.MasterData;

public interface MasterDataMapperService {

    MasterDataResponseDto toDto(MasterData masterData);
    MasterData toDomain(MasterDataRequestCreateDto masterDataRequestCreateDto);
}

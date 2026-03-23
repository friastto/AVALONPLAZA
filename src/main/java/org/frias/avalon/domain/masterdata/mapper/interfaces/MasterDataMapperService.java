package org.frias.avalon.domain.masterdata.mapper.interfaces;

import org.frias.avalon.domain.masterdata.dtos.request.MasterDataNewDto;
import org.frias.avalon.domain.masterdata.dtos.response.MasterDataResponseDto;
import org.frias.avalon.domain.masterdata.entities.MasterData;

public interface MasterDataMapperService {

    MasterDataResponseDto toDto(MasterData masterData);
    MasterData toDomain(MasterDataNewDto masterDataNewDto);
}

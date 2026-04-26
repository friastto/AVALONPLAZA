package org.frias.avalon.domain.masterdata.infraestructure.mapper;

import org.frias.avalon.domain.masterdata.application.dto.response.MasterDataResponseDto;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.infraestructure.persistence.entity.MasterData;

public interface MasterDataMapperService {

    MasterDataResponseDto toDto(MasterData masterData);

    MasterRoot toDomain(MasterData masterData);

    MasterData toEntity(MasterRoot masterRoot);

    MasterDataResponseDto toResponse(MasterRoot masterRoot);
}

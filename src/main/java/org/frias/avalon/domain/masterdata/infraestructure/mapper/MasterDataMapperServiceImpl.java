package org.frias.avalon.domain.masterdata.infraestructure.mapper;

import org.frias.avalon.domain.masterdata.application.dto.response.MasterDataResponseDto;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.infraestructure.persistence.entity.MasterData;
import org.springframework.stereotype.Service;

@Service
public class MasterDataMapperServiceImpl implements MasterDataMapperService {



    public MasterDataResponseDto toDto(MasterData masterData) {

        return new MasterDataResponseDto(
                masterData.getId()
                , masterData.getShortName()
                , masterData.getFullName()
        );
    }

    @Override
    public MasterRoot toDomain(MasterData entity) {

        return MasterRoot.fromPersistence(
                entity.getId(),
                entity.getShortName(),
                entity.getFullName(),
                entity.getParentId(),
                entity.getStatusId()
        );
    }

    @Override
    public MasterData toEntity(MasterRoot domain) {
        return MasterData.builder()
                .id(domain.getId())
                .shortName(domain.getShortName())
                .fullName(domain.getFullName())
                .parentId(domain.getParentId())
                .statusId(domain.getStatusId())
                .build();
    }

    @Override
    public MasterDataResponseDto toResponse(MasterRoot masterRoot) {

        return new MasterDataResponseDto(
                masterRoot.getId(),
                masterRoot.getShortName(),
                masterRoot.getFullName()
        );
    }


}

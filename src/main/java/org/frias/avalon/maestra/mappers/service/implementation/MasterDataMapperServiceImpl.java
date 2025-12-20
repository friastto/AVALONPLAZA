package org.frias.avalon.maestra.mappers.service.implementation;

import org.frias.avalon.maestra.dtos.MasterDataRequestCreateDto;
import org.frias.avalon.maestra.dtos.MasterDataResponseDto;
import org.frias.avalon.maestra.entities.MasterData;
import org.frias.avalon.maestra.mappers.service.interfaces.MasterDataMapperService;
import org.springframework.stereotype.Service;

@Service
public class MasterDataMapperServiceImpl implements MasterDataMapperService {


    @Override
    public MasterDataResponseDto toDto(MasterData masterData) {
        return new MasterDataResponseDto(
                masterData.getId()
                , masterData.getFullName()
                , masterData.getShortName()
        );
    }

    @Override
    public MasterData toDomain(MasterDataRequestCreateDto masterDataRequestCreateDto) {

        return null;
    }
}

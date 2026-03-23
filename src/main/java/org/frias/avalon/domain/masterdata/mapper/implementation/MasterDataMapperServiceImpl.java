package org.frias.avalon.domain.masterdata.mapper.implementation;

import org.frias.avalon.domain.masterdata.dtos.request.MasterDataNewDto;
import org.frias.avalon.domain.masterdata.dtos.response.MasterDataResponseDto;
import org.frias.avalon.domain.masterdata.entities.MasterData;
import org.frias.avalon.domain.masterdata.mapper.interfaces.MasterDataMapperService;
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
    public MasterData toDomain(MasterDataNewDto masterDataNewDto) {

        return null;
    }
}

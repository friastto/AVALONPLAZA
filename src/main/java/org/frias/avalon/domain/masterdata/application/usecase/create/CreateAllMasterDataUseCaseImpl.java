package org.frias.avalon.domain.masterdata.application.usecase.create;

import org.frias.avalon.domain.masterdata.application.dto.request.MasterDataNewDto;
import org.frias.avalon.domain.masterdata.application.dto.response.MasterDataResponseDto;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.repository.MasterDataRepositoryPort;
import org.frias.avalon.domain.masterdata.infraestructure.mapper.MasterDataMapperService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CreateAllMasterDataUseCaseImpl implements CreateAllMasterDataUseCase {

    private final MasterDataRepositoryPort masterDataRepositoryPort;
    private final MasterDataMapperService mapper;

    public CreateAllMasterDataUseCaseImpl(MasterDataRepositoryPort masterDataRepositoryPort, MasterDataMapperService mapper) {
        this.masterDataRepositoryPort = masterDataRepositoryPort;
        this.mapper = mapper;
    }


    @Override
    public List<MasterDataResponseDto> execute(List<MasterDataNewDto> request) {

        List<MasterDataResponseDto> mdList = new ArrayList<>();

        for(MasterDataNewDto dto : request) {

            Long parentId = masterDataRepositoryPort.getIdByCode(dto.parentShortName());

            Long statusId = masterDataRepositoryPort.getIdByCode("ACT");

            MasterRoot domain = MasterRoot.create(dto.shortName(), dto.fullName(), parentId, statusId);

           mdList.add( mapper.toResponse(masterDataRepositoryPort.save(domain)));
        }
        return mdList;
    }
}

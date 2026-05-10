package org.frias.avalon.domain.masterdata.application.usecase.find;

import org.frias.avalon.domain.masterdata.application.dto.response.MasterDataResponseDto;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.repository.MasterDataRepositoryPort;
import org.frias.avalon.domain.masterdata.infraestructure.mapper.MasterDataMapperService;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.hibernate.spatial.dialect.hana.HANASpatialFunctions.m;

@Service
public class FindAllMasterDataUseCaseImpl implements FindAllMasterDataUseCase{

    private final MasterDataRepositoryPort masterPort;
    private final MasterDataMapperService mapper;


    public FindAllMasterDataUseCaseImpl(MasterDataRepositoryPort masterPort, MasterDataMapperService mapper) {
        this.masterPort = masterPort;
        this.mapper = mapper;
    }

    @Override
    public List<MasterDataResponseDto> execute() {

        List<MasterRoot> masterRootList = masterPort.findAll();


        return masterRootList.stream().map(mapper::toResponse).toList();
    }
}
package org.frias.avalon.domain.masterdata.application.usecase.find;

import jakarta.persistence.EntityNotFoundException;
import org.frias.avalon.domain.masterdata.application.dto.response.MasterDataResponseDto;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.repository.MasterDataRepositoryPort;
import org.frias.avalon.domain.masterdata.infraestructure.mapper.MasterDataMapperService;
import org.springframework.stereotype.Service;

@Service
public class FindMasterDataByIdUseCaseImpl implements FindMasterDataByIdUseCase{

    private final MasterDataRepositoryPort masterDataRepositoryPort;
    private final MasterDataMapperService mapper;

    public FindMasterDataByIdUseCaseImpl(MasterDataRepositoryPort masterDataRepositoryPort, MasterDataMapperService mapper) {
        this.masterDataRepositoryPort = masterDataRepositoryPort;
        this.mapper = mapper;
    }

    @Override
    public MasterDataResponseDto execute(Long id) {

        MasterRoot mr =  masterDataRepositoryPort.findById(id).orElseThrow(()->new EntityNotFoundException("No existe la clave MasterData"));

        return mapper.toResponse(mr);
    }
}

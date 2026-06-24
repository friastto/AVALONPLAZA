package org.frias.avalon.domain.masterdata.application.usecase.find;

import org.frias.avalon.domain.masterdata.application.dto.response.MasterDataResponseDto;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.repository.MasterDataRepositoryPort;
import org.frias.avalon.domain.masterdata.infraestructure.mapper.MasterDataMapperService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FindMasterDataChildrenByParentCodeUseCaseImpl implements FindMasterDataChildrenByParentCodeUseCase {

    private final MasterDataRepositoryPort masterDataRepositoryPort;
    private final MasterDataMapperService mapper;

    public FindMasterDataChildrenByParentCodeUseCaseImpl(MasterDataRepositoryPort masterDataRepositoryPort, MasterDataMapperService mapper) {
        this.masterDataRepositoryPort = masterDataRepositoryPort;
        this.mapper = mapper;
    }

    @Override
    public List<MasterDataResponseDto> execute(String parentCode) {
        List<MasterRoot> children = masterDataRepositoryPort.findChildrenByParentCode(parentCode);


        return children.stream()
                .map(mapper::toResponse)
                .toList();
    }
}
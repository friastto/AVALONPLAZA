package org.frias.avalon.domain.masterdata.application.usecase.create;

import org.frias.avalon.domain.masterdata.application.dto.request.MasterDataNewDto;
import org.frias.avalon.domain.masterdata.application.dto.response.MasterDataResponseDto;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.repository.MasterDataRepositoryPort;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.masterdata.infraestructure.mapper.MasterDataMapperService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CreateAllMasterDataUseCaseImpl implements CreateAllMasterDataUseCase {

    private final MasterDataRepositoryPort masterDataRepositoryPort;
    private final MasterDataMapperService mapper;
    private final MasterTreeProvider masterTreeProvider;

    public CreateAllMasterDataUseCaseImpl(MasterDataRepositoryPort masterDataRepositoryPort, MasterDataMapperService mapper, MasterTreeProvider masterTreeProvider) {
        this.masterDataRepositoryPort = masterDataRepositoryPort;
        this.mapper = mapper;
        this.masterTreeProvider = masterTreeProvider;
    }


    @Override
    public List<MasterDataResponseDto> execute(List<MasterDataNewDto> request) {

        List<MasterDataResponseDto> mdList = new ArrayList<>();

        MasterRoot statusNode = masterTreeProvider.getTree() != null
                ? masterTreeProvider.getTree().getByCode("ACT")
                : null;
        Long statusId = statusNode != null ? statusNode.getId() : masterDataRepositoryPort.getIdByCode("ACT");

        for (MasterDataNewDto dto : request) {

            Long parentId = null;
            if (dto.parentShortName() != null && !dto.parentShortName().isBlank()) {
                MasterRoot parentNode = masterTreeProvider.getTree() != null
                        ? masterTreeProvider.getTree().getByCode(dto.parentShortName().trim().toUpperCase())
                        : null;
                parentId = parentNode != null ? parentNode.getId() : masterDataRepositoryPort.getIdByCode(dto.parentShortName().trim().toUpperCase());
            }

            MasterRoot domain = MasterRoot.create(
                    dto.shortName().trim().toUpperCase(),
                    dto.fullName().trim().toUpperCase(),
                    parentId,
                    statusId);

            mdList.add(mapper.toResponse(masterDataRepositoryPort.save(domain)));
        }

        masterTreeProvider.refresh();
        return mdList;
    }
}

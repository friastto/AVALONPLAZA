package org.frias.avalon.domain.masterdata.application.usecase.changestatus;

import jakarta.persistence.EntityNotFoundException;
import org.frias.avalon.domain.masterdata.application.dto.request.MasterDataUpdateStatusDto;
import org.frias.avalon.domain.masterdata.application.dto.response.MasterDataResponseDto;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.StatusRules;
import org.frias.avalon.domain.masterdata.domain.repository.MasterDataRepositoryPort;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.masterdata.infraestructure.mapper.MasterDataMapperService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChangeStatusUseCaseImpl implements ChangeStatusUseCase {

    private final MasterDataRepositoryPort masterDataRepositoryPort;
    private final MasterDataMapperService mapper;
    private final MasterTreeProvider masterTreeProvider;


    public ChangeStatusUseCaseImpl(MasterDataRepositoryPort masterDataRepositoryPort, MasterDataMapperService mapper, MasterTreeProvider masterTreeProvider) {
        this.masterDataRepositoryPort = masterDataRepositoryPort;
        this.mapper = mapper;
        this.masterTreeProvider = masterTreeProvider;
    }

    @Transactional
    @Override
    public MasterDataResponseDto execute(MasterDataUpdateStatusDto dataDto) {

        // isMasterStaff();

        MasterRoot current = masterDataRepositoryPort.findById(dataDto.current()).orElseThrow(() -> new EntityNotFoundException("No existe el tipo a actualizar MasterData"));

        MasterRoot next = masterDataRepositoryPort.findById(dataDto.next()).orElseThrow(() -> new EntityNotFoundException("No existe la clave MasterData"));

        StatusRules.validateTransition(current, next);

        current.changeStatus(next.getId());

        MasterRoot mrUpdated = masterDataRepositoryPort.save(current);

        masterTreeProvider.refresh();

        return mapper.toResponse(mrUpdated);
    }
}

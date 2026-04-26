package org.frias.avalon.domain.masterdata.application.usecase.changestatus;

import jakarta.persistence.EntityNotFoundException;
import org.frias.avalon.domain.masterdata.application.dto.request.MasterDataUpdateStatusDto;
import org.frias.avalon.domain.masterdata.application.dto.response.MasterDataResponseDto;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.StatusRules;
import org.frias.avalon.domain.masterdata.domain.repository.MasterDataRepositoryPort;
import org.frias.avalon.domain.masterdata.infraestructure.mapper.MasterDataMapperService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChangeStatusUseCaseImpl implements ChangeStatusUseCase {

    private final MasterDataRepositoryPort masterDataRepositoryPort;
    private final MasterDataMapperService mapper;


    public ChangeStatusUseCaseImpl(MasterDataRepositoryPort masterDataRepositoryPort, MasterDataMapperService mapper) {
        this.masterDataRepositoryPort = masterDataRepositoryPort;
        this.mapper = mapper;
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

        return mapper.toResponse(mrUpdated);
    }
}

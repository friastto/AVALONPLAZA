package org.frias.avalon.domain.user.application.usecase.find;

import jakarta.persistence.EntityNotFoundException;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.repository.MasterDataRepositoryPort;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.user.application.dtos.response.UserAvalonResponseDto;
import org.frias.avalon.domain.user.domain.mapper.UserAvalonMapper;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.frias.avalon.domain.user.domain.port.UserAvalonRepositoryPort;
import org.springframework.stereotype.Service;

@Service
public class FindByPersonNumberIdCaseImpl implements FindByPersonNumberIdUseCase {

    private final UserAvalonRepositoryPort userPort;
    private final UserAvalonMapper mapper;
    private final MasterDataRepositoryPort masterPort;
    private final MasterTreeProvider masterTreeProvider;

    public FindByPersonNumberIdCaseImpl(UserAvalonRepositoryPort userPort, UserAvalonMapper mapper, MasterDataRepositoryPort masterPort, MasterTreeProvider masterTreeProvider) {
        this.userPort = userPort;
        this.mapper = mapper;
        this.masterPort = masterPort;
        this.masterTreeProvider = masterTreeProvider;
    }


    @Override
    public UserAvalonResponseDto execute(String numberId) {
        Long nummberOfIdentification = Long.valueOf(numberId);

        UserAvalonDomain user = userPort.findByUserName(numberId)
                .orElseThrow(() -> new EntityNotFoundException("usuario no encontrado"));

        var tree = masterTreeProvider.getTree();

        MasterRoot status = tree.getById(user.getStatusId());

        if (status == null) {
            throw new IllegalStateException("Estado inconsistente en cache");
        }

        return mapper.toResponse(user, status);
    }
}

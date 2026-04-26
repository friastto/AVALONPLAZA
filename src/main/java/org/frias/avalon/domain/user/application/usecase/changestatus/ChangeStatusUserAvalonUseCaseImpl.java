package org.frias.avalon.domain.user.application.usecase.changestatus;

import org.frias.avalon.domain.user.application.dtos.request.ChangeUserAvalonStatusRequest;
import org.frias.avalon.domain.user.application.dtos.response.UserAvalonResponseDto;
import org.frias.avalon.domain.user.domain.port.UserAvalonRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class ChangeStatusUserAvalonUseCaseImpl implements ChangeStatusUserAvalonUseCase {

    private final UserAvalonRepositoryPort userAvalonPort;

    public ChangeStatusUserAvalonUseCaseImpl(UserAvalonRepositoryPort userAvalonPort) {
        this.userAvalonPort = userAvalonPort;
    }

    @Transactional
    @Override
    public UserAvalonResponseDto execute(ChangeUserAvalonStatusRequest request) {





        return null;
    }
}

package org.frias.avalon.domain.user.application.usecase.login;

import org.frias.avalon.domain.masterdata.domain.repository.MasterDataRepositoryPort;
import org.frias.avalon.domain.user.application.dtos.request.AuthRequest;
import org.frias.avalon.domain.user.application.dtos.response.AuthResponse;
import org.frias.avalon.domain.user.domain.port.UserAvalonRepositoryPort;

public class LoginUseCaseImpl implements LoginUseCase{

    private final UserAvalonRepositoryPort userPort;
    private final MasterDataRepositoryPort masterPort;
    //private final RoleAssignmentRepositoryPort roleAssignmentPort;

    public LoginUseCaseImpl(UserAvalonRepositoryPort userPort, MasterDataRepositoryPort masterPort) {
        this.userPort = userPort;
        this.masterPort = masterPort;
    }


    @Override
    public AuthResponse execute(AuthRequest request) {
        return null;
    }
}

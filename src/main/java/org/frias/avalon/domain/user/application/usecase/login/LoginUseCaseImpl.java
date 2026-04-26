package org.frias.avalon.domain.user.application.usecase.login;

import jakarta.persistence.EntityNotFoundException;
import org.frias.avalon.core.validation.PassSecure;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.repository.MasterDataRepositoryPort;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.user.application.dtos.request.AuthRequest;
import org.frias.avalon.domain.user.application.dtos.request.AuthorizationResult;
import org.frias.avalon.domain.user.application.dtos.response.AuthResponse;
import org.frias.avalon.domain.user.application.dtos.response.UserAvalonResponseDto;
import org.frias.avalon.domain.user.application.service.AuthorizationMachine;
import org.frias.avalon.domain.user.domain.mapper.UserAvalonMapper;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.frias.avalon.domain.user.domain.port.UserAvalonRepositoryPort;
import org.springframework.stereotype.Service;

@Service
public class LoginUseCaseImpl implements LoginUseCase{

    //private final RoleAssignmentRepositoryPort roleAssignmentPort;

    private final UserAvalonRepositoryPort userPort;
    private final MasterTreeProvider masterTreeProvider;
    private final UserAvalonMapper mapper;
    private final  AuthorizationMachine authMachine;

    public LoginUseCaseImpl(UserAvalonRepositoryPort userPort, MasterTreeProvider masterTreeProvider, UserAvalonMapper mapper, AuthorizationMachine authMachine) {
        this.userPort = userPort;
        this.masterTreeProvider = masterTreeProvider;
        this.mapper = mapper;
        this.authMachine = authMachine;
    }


    @Override
    public AuthResponse execute(AuthRequest request) {


        UserAvalonDomain user = userPort.findByUserNmae(request.username())
                .orElseThrow(() -> new EntityNotFoundException("usuario no encontrado"));

        var tree = masterTreeProvider.getTree();

        MasterRoot status = tree.getById(user.getStatusId());

        if (status == null) {
            throw new IllegalStateException("Estado inconsistente en cache");
        }

        if (tree.is(status, "BAN") || tree.is(status, "LOKUSER")) {
            throw new IllegalStateException("Usuario no puede autenticarse");
        }

       if( PassSecure.verifyPassword(request.password(), user.getHashSalt(),user.getHashPassword()))
           throw new IllegalStateException("Credenciales inválidas");

        AuthorizationResult authz = authMachine.resolve(user);


        UserAvalonResponseDto userDto = mapper.toResponse(user,status);

        return new AuthResponse(
                "token generated null",
                userDto,
                authz.roles(),

                authz.permissions()
        );
    }
}

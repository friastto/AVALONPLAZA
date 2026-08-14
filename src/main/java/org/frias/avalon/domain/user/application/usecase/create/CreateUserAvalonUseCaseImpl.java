package org.frias.avalon.domain.user.application.usecase.create;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.frias.avalon.core.validation.PassSecure;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.repository.MasterDataRepositoryPort;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.user.application.dtos.request.UserNewDto;
import org.frias.avalon.domain.user.application.dtos.response.UserAvalonResponseDto;
import org.frias.avalon.domain.user.infraestructure.persistence.mapper.UserAvalonMapper;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.frias.avalon.domain.user.domain.port.UserAvalonRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateUserAvalonUseCaseImpl implements CreateUserAvalonUseCase {

    private final UserAvalonRepositoryPort userPort;
    private final MasterDataRepositoryPort mdPort;
    private final UserAvalonMapper mapper;
    private final MasterTreeProvider masterTreeProvider;

    public CreateUserAvalonUseCaseImpl(UserAvalonRepositoryPort userPort, MasterDataRepositoryPort mdPort, UserAvalonMapper mapper, MasterTreeProvider masterTreeProvider) {
        this.userPort = userPort;
        this.mdPort = mdPort;
        this.mapper = mapper;
        this.masterTreeProvider = masterTreeProvider;
    }

    @Transactional
    @Override
    public UserAvalonResponseDto execute(UserNewDto request) {

        if (userPort.existByUsername(request.userName())) {

            throw new EntityExistsException("Nombre de usuario no disponible");
        }

        MasterRoot statusActive = mdPort.getActiveStatus()
                .orElseThrow(() -> new EntityNotFoundException("no se puede Activar el suaurio qeu se quiere crear"));

        MasterTree tree = masterTreeProvider.getTree();

        if (!tree.isChildOf(statusActive, "STSGEN"))
            throw new RuntimeException("no se pudo establecer el estado del usuario");


        String salt = PassSecure.generateSalt();


        String hashPasword = PassSecure.hashPassword(request.password(), salt);

        UserAvalonDomain newUser = userPort.save(UserAvalonDomain.create(
                        request.userName(),
                        salt,
                        hashPasword,
                        statusActive.getId()
                )
        );

        return mapper.toResponse(newUser, statusActive);
    }
}

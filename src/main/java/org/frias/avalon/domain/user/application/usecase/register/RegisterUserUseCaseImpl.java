package org.frias.avalon.domain.user.application.usecase.register;

import jakarta.persistence.EntityNotFoundException;
import org.frias.avalon.core.exeptions.BusinessException;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.repository.MasterDataRepositoryPort;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.user.application.dtos.request.AuthRequest;
import org.frias.avalon.domain.user.application.dtos.request.FullPersonAndUser;
import org.frias.avalon.domain.user.application.dtos.response.AuthResponse;
import org.frias.avalon.domain.user.application.dtos.response.UserAvalonResponseDto;
import org.frias.avalon.domain.user.application.usecase.asignmentPerson.AssignPersonToUserUseCase;
import org.frias.avalon.domain.user.application.usecase.create.CreateUserAvalonUseCase;
import org.frias.avalon.domain.user.application.usecase.login.LoginUseCase;
import org.frias.avalon.domain.user.domain.model.RoleAssignmentDomain;
import org.frias.avalon.domain.user.domain.port.RoleAssignmentRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterUserUseCaseImpl implements RegisterUserUseCase {

    private final CreateUserAvalonUseCase createUserUseCase;
    private final AssignPersonToUserUseCase assignPersonToUserUseCase;
    private final LoginUseCase loginUseCase;
    private final MasterDataRepositoryPort masterDataRepositoryPort;
    private final RoleAssignmentRepositoryPort roleAssignmentRepositoryPort;
    private final MasterTreeProvider masterTreeProvider;

    public RegisterUserUseCaseImpl(
            CreateUserAvalonUseCase createUserUseCase,
            AssignPersonToUserUseCase assignPersonToUserUseCase,
            LoginUseCase loginUseCase,
            MasterDataRepositoryPort masterDataRepositoryPort,
            RoleAssignmentRepositoryPort roleAssignmentRepositoryPort,
            MasterTreeProvider masterTreeProvider) {
        this.createUserUseCase = createUserUseCase;
        this.assignPersonToUserUseCase = assignPersonToUserUseCase;
        this.loginUseCase = loginUseCase;
        this.masterDataRepositoryPort = masterDataRepositoryPort;
        this.roleAssignmentRepositoryPort = roleAssignmentRepositoryPort;
        this.masterTreeProvider = masterTreeProvider;
    }

    @Transactional
    @Override
    public AuthResponse execute(FullPersonAndUser request) {
        // 1. Crear el Usuario (credenciales)
        UserAvalonResponseDto user = createUserUseCase.execute(request.userAvalon());

        // 2. Crear y Asignar la Persona al Usuario
        assignPersonToUserUseCase.execute(user.id(), request.person());

        // 3. Validar y crear la asignación de rol
        MasterTree tree = masterTreeProvider.getTree();
        MasterRoot roleToAssign;

        if (request.roleId() == null) {
            // Asignar rol por defecto "Consumidor Estándar"
            roleToAssign = masterDataRepositoryPort.findByCode("CSTNDR")
                    .orElseThrow(() -> new EntityNotFoundException("El rol por defecto 'CSTNDR' no está configurado en el sistema."));

            if (!tree.isChildOf(roleToAssign, "ROL")) {
                throw new BusinessException("El ID proporcionado no corresponde a un rol válido.");
            }
        } else {
            // Validar el rol proporcionado
            roleToAssign = tree.getById(request.roleId());
            if (roleToAssign == null) {
                throw new BusinessException("El rol con ID " + request.roleId() + " no es válido.");
            }
            if (!tree.isChildOf(roleToAssign, "ROL")) {
                throw new BusinessException("El ID proporcionado no corresponde a un rol válido.");
            }
        }
        
        // Un rol de consumidor no debe tener un outletId
        if (tree.isChildOf(roleToAssign, "CONS") && request.outletId() != null) {
            throw new BusinessException("Un rol de consumidor no puede ser asignado a una tienda (outlet).");
        }

        MasterRoot activeStatus = masterDataRepositoryPort.getActiveStatus()
                .orElseThrow(() -> new EntityNotFoundException("El estado 'ACT' no está configurado en el sistema."));

        RoleAssignmentDomain newAssignment = RoleAssignmentDomain.create(user.id(), roleToAssign.getId(), request.outletId(), activeStatus.getId());
        roleAssignmentRepositoryPort.create(newAssignment);

        // 4. Autenticar al usuario recién creado para devolver los tokens
        AuthRequest authRequest = new AuthRequest(
                request.userAvalon().userName(),
                request.userAvalon().password()
        );
        
        return loginUseCase.execute(authRequest);
    }
}
package org.frias.avalon.domain.user.application.usecase.assingnrole;

import jakarta.persistence.EntityNotFoundException;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.repository.MasterDataRepositoryPort;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.user.application.dtos.request.AssignmentRoleRequestDto;
import org.frias.avalon.domain.user.application.dtos.response.AssignmentRoleResponse;
import org.frias.avalon.domain.user.domain.mapper.RoleAssignmentMapper;
import org.frias.avalon.domain.user.domain.model.RoleAssignmentDomain;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.frias.avalon.domain.user.domain.port.RoleAssignmentRepositoryPort;
import org.frias.avalon.domain.user.domain.port.UserAvalonRepositoryPort;
import org.springframework.stereotype.Service;

@Service
public class AssignmentRoleUseCaseImpl implements AssignmentRoleUseCase{

    private final UserAvalonRepositoryPort userPort;
    private final RoleAssignmentRepositoryPort rolePort;
    private final MasterDataRepositoryPort masterPort;
    private final RoleAssignmentMapper mapper;
    private final MasterTreeProvider treeProvider;

    public AssignmentRoleUseCaseImpl(UserAvalonRepositoryPort userPort, RoleAssignmentRepositoryPort rolePort, MasterDataRepositoryPort masterPort, RoleAssignmentMapper mapper, MasterTreeProvider treeProvider) {
        this.userPort = userPort;
        this.rolePort = rolePort;
        this.masterPort = masterPort;
        this.mapper = mapper;
        this.treeProvider = treeProvider;
    }

    @Override
    public AssignmentRoleResponse execute(AssignmentRoleRequestDto request) {

        UserAvalonDomain user = userPort.findById(request.userId())
                    .orElseThrow(()-> new EntityNotFoundException("no se pudo encontrar el usuario"));

        MasterRoot userStatus = masterPort.findById(user.getStatusId())
                .orElseThrow(()-> new EntityNotFoundException("no se pudo validar el estado del usuario"));


        MasterRoot role = masterPort.findById(request.roleId())
                .orElseThrow(()-> new EntityNotFoundException("no se pudo encontrar el rol para asignar"));

        var tree = treeProvider.getTree();

        // 1. Validar que es rol
        if (!tree.isChildOf(role, "ROL")) {
            throw new RuntimeException("No es un rol válido");
        }

        // 2. Reglas de negocio
        if (tree.isChildOf(role, "CLIENTE") && request.scope() != null) {
            throw new RuntimeException("Cliente no debe tener scope");
        }

        if (tree.isChildOf(role, "OPT") && request.scope() == null) {
            throw new RuntimeException("Rol operativo requiere scope");
        }

        MasterRoot statusActive = masterPort.getActiveStatus()
                .orElseThrow(()-> new EntityNotFoundException("no se pudo activar el rol al usuario"));


        RoleAssignmentDomain.create(user.getId(),role.getId(),1L,2L,statusActive.getId());

        return  mapper.toResponse(user,userStatus, role, statusActive,"empresa", "outlet");
    }
}

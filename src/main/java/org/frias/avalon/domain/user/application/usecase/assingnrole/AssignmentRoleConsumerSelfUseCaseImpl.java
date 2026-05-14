package org.frias.avalon.domain.user.application.usecase.assingnrole;

import com.twelvemonkeys.imageio.metadata.tiff.IFD;
import jakarta.persistence.EntityNotFoundException;
import org.frias.avalon.core.exeptions.BusinessException;
import org.frias.avalon.core.tenant.TenantContext;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.repository.MasterDataRepositoryPort;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.user.application.dtos.response.AssignmentRoleResponse;
import org.frias.avalon.domain.user.application.service.PermissionService;
import org.frias.avalon.domain.user.domain.mapper.RoleAssignmentMapper;
import org.frias.avalon.domain.user.domain.model.RoleAssignmentDomain;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.frias.avalon.domain.user.domain.port.RoleAssignmentRepositoryPort;
import org.frias.avalon.domain.user.domain.port.UserAvalonRepositoryPort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AssignmentRoleConsumerSelfUseCaseImpl implements AssignmentRoleConsumerSelfUseCase {
private final PermissionService permissionService;
private final UserAvalonRepositoryPort userPort;
private final MasterDataRepositoryPort masterPort;
private final MasterTreeProvider treeProvider;
private final RoleAssignmentRepositoryPort rolePort;
private final RoleAssignmentMapper mapper;

    public AssignmentRoleConsumerSelfUseCaseImpl(PermissionService permissionService, UserAvalonRepositoryPort userPort, MasterDataRepositoryPort masterPort, MasterTreeProvider treeProvider, RoleAssignmentRepositoryPort rolePort, RoleAssignmentMapper mapper) {
        this.permissionService = permissionService;
        this.userPort = userPort;
        this.masterPort = masterPort;
        this.treeProvider = treeProvider;
        this.rolePort = rolePort;
        this.mapper = mapper;
    }

    @Transactional
    @Override
    public AssignmentRoleResponse execute() {



        if (permissionService.canAutoAssignConsumerRole()) throw new BusinessException("No tiene los permisos necesarios para la activacion del rol de consumidor");


        String currentUserName = SecurityContextHolder.getContext().getAuthentication().getName();
        UserAvalonDomain user = userPort.findByUserName(currentUserName)
                .orElseThrow(()-> new EntityNotFoundException("no se pudo encontrar el usuario"));

        MasterRoot userStatus = masterPort.findById(user.getStatusId())
                .orElseThrow(()-> new EntityNotFoundException("no se pudo validar el estado del usuario"));

        MasterRoot role = masterPort.findByCode("CSTNDR")
                .orElseThrow(()-> new EntityNotFoundException("no se pudo encontrar el rol para asignar"));

        var tree = treeProvider.getTree();

        // 1. Validar que es rol
        if (!tree.isChildOf(role, "ROL")) {
            throw new RuntimeException("No es un rol válido");
        }
        if (!tree.isChildOf(role, "CONS")) {
            throw new RuntimeException("no se puede activar el rol de Consumidor");
        }
        List<RoleAssignmentDomain> roleAssigned = rolePort.findByUser(user.getId());

        RoleAssignmentDomain consumerRole = roleAssigned.stream()
                .filter(assignment ->
                        assignment.getOutletId() != null
                                && tree.isChildOf(
                                tree.getById(assignment.getRoleId()),
                                "CONS")
                                && tree.is(tree.getById(assignment.getStatus()),"ACT")

                )
                .findFirst()
                .orElse(null);

        MasterRoot currentRoleStatus = null;
        if(consumerRole != null) {
            currentRoleStatus = tree.getById(consumerRole.getStatus());
            if (currentRoleStatus.isActive("ACT")) {
                return mapper.toResponse(user, userStatus, role, currentRoleStatus, null);

            }else {throw new IllegalStateException("usted tiene una cuenta de consumidor desactivada o bloqueada");}

        }


            MasterRoot statusActive = masterPort.getActiveStatus()
                    .orElseThrow(() -> new EntityNotFoundException("no se pudo activar el rol al usuario"));

            rolePort.create(RoleAssignmentDomain.create(
                            user.getId(), role.getId(), null, statusActive.getId()
                    )
            );

            return mapper.toResponse(user, userStatus, role, statusActive, null);

    }
}

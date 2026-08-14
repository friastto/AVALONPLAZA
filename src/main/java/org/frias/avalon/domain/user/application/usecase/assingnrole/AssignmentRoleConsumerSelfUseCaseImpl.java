package org.frias.avalon.domain.user.application.usecase.assingnrole;

import jakarta.persistence.EntityNotFoundException;
import org.frias.avalon.core.exeptions.BusinessException;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.repository.MasterDataRepositoryPort;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.user.application.dtos.response.AssignmentRoleResponse;
import org.frias.avalon.domain.user.application.service.PermissionService;
import org.frias.avalon.domain.user.infraestructure.persistence.mapper.RoleAssignmentMapper;
import org.frias.avalon.domain.user.domain.model.RoleAssignmentDomain;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.frias.avalon.domain.user.domain.port.RoleAssignmentRepositoryPort;
import org.frias.avalon.domain.user.domain.port.UserAvalonRepositoryPort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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
        String currentUserName = SecurityContextHolder.getContext().getAuthentication().getName();

        // --- 1. Obtener el usuario actual ---
        UserAvalonDomain user = userPort.findByUserName(currentUserName)
                .orElseThrow(() -> new EntityNotFoundException("No se pudo encontrar el usuario actual."));

        MasterRoot userStatus = masterPort.findById(user.getStatusId())
                .orElseThrow(() -> new EntityNotFoundException("No se pudo validar el estado del usuario actual."));

        // --- 2. Definir el rol a auto-asignar (CSTNDR) ---
        MasterRoot roleToAssign = masterPort.findByCode("CSTNDR")
                .orElseThrow(() -> new EntityNotFoundException("No se pudo encontrar el rol 'CSTNDR' para asignar."));

        // --- 3. Validar permiso para auto-asignar este rol ---
        // El PermissionService verifica si el usuario actual (ANONIMO/CSTNDR) puede auto-asignarse CSTNDR
        if (!permissionService.canAutoAssignConsumerRole(roleToAssign)) {
            throw new BusinessException("Acceso denegado: No tiene los permisos necesarios para auto-asignarse el rol de consumidor.");
        }
// --- 4. Validaciones del rol a asignar (invariantes) ---
        var tree = treeProvider.getTree();
        if (!tree.isChildOf(roleToAssign, "ROL")) {
            throw new BusinessException("El rol 'CSTNDR' no es un rol válido en la jerarquía.");
        }
        if (!tree.isChildOf(roleToAssign, "CONS")) {
            throw new BusinessException("El rol 'CSTNDR' no es un rol de Consumidor válido.");
        }

        // --- 5. Verificar si el usuario ya tiene el rol CSTNDR activo ---
        Optional<RoleAssignmentDomain> existingCstndrAssignment = rolePort.findByUserAvalonId(user.getId()).stream()
                .filter(assignment -> {
                    MasterRoot assignedRole = tree.getById(assignment.getRoleId());
                    MasterRoot assignmentStatus = tree.getById(assignment.getStatus());
                    return assignedRole != null && assignedRole.getShortName().equals("CSTNDR");
                })
                .findFirst();
        if (existingCstndrAssignment.isPresent()) {
            RoleAssignmentDomain consumerAssignment = existingCstndrAssignment.get();
            MasterRoot currentAssignmentStatus = tree.getById(consumerAssignment.getStatus());

            if (currentAssignmentStatus == null) {
                throw new IllegalStateException("Estado de asignación de rol de consumidor inconsistente.");
            }

            if (currentAssignmentStatus.is("ACT")) { // Si el estado es ACTIVO
                // Ya tiene el rol CSTNDR activo, simplemente devolvemos la información existente
                return mapper.toResponse(user, userStatus, roleToAssign, currentAssignmentStatus, null);
            } else {
                // Tiene el rol CSTNDR pero no está activo
                throw new BusinessException("Usted ya tiene el rol de consumidor (" + roleToAssign.getFullName() + ") pero se encuentra en estado " + currentAssignmentStatus.getFullName() + ". No puede auto-asignarse un nuevo rol.");
            }
        }

        // --- 6. Crear la nueva asignación de rol ---
        MasterRoot statusActive = masterPort.getActiveStatus()
                .orElseThrow(() -> new EntityNotFoundException("No se pudo obtener el estado 'ACTIVO' para la asignación del rol."));

        rolePort.create(RoleAssignmentDomain.create(
                user.getId(), roleToAssign.getId(), null, statusActive.getId() // null para outletId de consumidor
        ));

        // --- 7. Construir y devolver la respuesta ---
        return mapper.toResponse(user, userStatus, roleToAssign, statusActive, null);
    }
}
/*
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


 */
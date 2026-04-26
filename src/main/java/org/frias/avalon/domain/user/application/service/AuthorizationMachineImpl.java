package org.frias.avalon.domain.user.application.service;

import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.user.application.dtos.request.AuthorizationResult;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.frias.avalon.domain.user.domain.port.RoleAssignmentRepositoryPort;
import org.frias.avalon.domain.user.domain.port.UserAvalonRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class AuthorizationMachineImpl implements AuthorizationMachine {

    private final RoleAssignmentRepositoryPort roleAssignmentPort;
    private final MasterTreeProvider treeProvider;
    private final PermissionService permissionService;
    private final UserAvalonRepositoryPort userPort;

    public AuthorizationMachineImpl(
            RoleAssignmentRepositoryPort roleAssignmentPort,
            MasterTreeProvider treeProvider,
            PermissionService permissionService, UserAvalonRepositoryPort userPort
    ) {
        this.roleAssignmentPort = roleAssignmentPort;
        this.treeProvider = treeProvider;
        this.permissionService = permissionService;
        this.userPort = userPort;
    }

    @Override
    public AuthorizationResult resolve(UserAvalonDomain user) {

        var tree = treeProvider.getTree();

        // 1. Obtener asignaciones
        var assignments = roleAssignmentPort.findByUser(user.getId());

        // 2. Obtener roles (MasterRoot)
        List<MasterRoot> roles = assignments.stream()
                .map(a -> tree.getById(a.getRoleId()))
                .filter(Objects::nonNull)
                .toList();


        if (roles.isEmpty()) {

            MasterRoot anonRole = tree.getByCode("USANONIMO");

            if (anonRole == null) {
                throw new IllegalStateException("Rol anónimo no configurado");
            }
            roles = List.of(anonRole);
        }
        // 3. Resolver permisos
        List<String> permissions = permissionService.resolvePermissions(roles);

        // 4. Mapear roles a códigos
        List<String> roleCodes = roles.stream()
                .map(MasterRoot::getShortName)
                .toList();

        return new AuthorizationResult(roleCodes, permissions, List.of());
    }
}

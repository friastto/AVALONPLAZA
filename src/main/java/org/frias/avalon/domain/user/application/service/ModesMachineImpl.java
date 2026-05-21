package org.frias.avalon.domain.user.application.service;

import org.frias.avalon.domain.masterdata.application.dto.response.MasterDataResponseDto;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.outlet.application.dto.response.OutletInfoDto;
import org.frias.avalon.domain.outlet.domain.model.OutletDomain;
import org.frias.avalon.domain.outlet.domain.port.OutletRepositoryPort;
import org.frias.avalon.domain.user.application.dtos.response.modes.ClientModeDto;
import org.frias.avalon.domain.user.application.dtos.response.modes.EmployeeModeDto;
import org.frias.avalon.domain.user.application.dtos.response.modes.ModesResponseDto;
import org.frias.avalon.domain.user.application.dtos.results.ClientResult;
import org.frias.avalon.domain.user.application.dtos.results.EmployeeResult;
import org.frias.avalon.domain.user.application.dtos.results.ModesResult;
import org.frias.avalon.domain.user.domain.model.RoleAssignmentDomain;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class ModesMachineImpl implements ModesMachine {

    private final MasterTreeProvider treeProvider;
    private final PermissionService permissionService;
    private final OutletRepositoryPort outletPort; // No se usa directamente en resolve, pero se mantiene si es necesario en otros métodos

    public ModesMachineImpl(MasterTreeProvider treeProvider, PermissionService permissionService, OutletRepositoryPort outletPort) {
        this.treeProvider = treeProvider;
        this.permissionService = permissionService;
        this.outletPort = outletPort;
    }

    @Override
    public ModesResult resolve(List<RoleAssignmentDomain> byUser, OutletDomain outletDomain) {
        MasterTree tree = treeProvider.getTree();

        ClientResult clientResult = null;
        EmployeeResult employeeResult = null;

        // --- Manejo del escenario de usuario sin roles asignados (cliente anónimo) ---
        if (byUser.isEmpty()) {
            // Asumimos que "ANONIMO" es el shortName para el rol de cliente anónimo en MasterData
            MasterRoot anonymousRole = tree.getByCode("USANONIMO"); // Buscar el MasterRoot para "ANONIMO"
            List<String> anonymousPermissions = Collections.emptyList(); // Por defecto, cliente anónimo no tiene permisos específicos

            if (anonymousRole != null) {

                anonymousPermissions = permissionService.resolvePermissions(anonymousRole);
                clientResult = new ClientResult(
                        anonymousRole.getFullName(),
                        true, // Cliente anónimo está "activo" por defecto
                        anonymousPermissions
                );
            } else {
                // Fallback si el rol "ANONIMO" no se encuentra en el MasterTree
                clientResult = new ClientResult(
                        "Cliente Anónimo",
                        true,
                        Collections.emptyList()
                );
            }
        } else {
            // --- Procesar roles asignados si la lista no está vacía ---
            for (RoleAssignmentDomain roleAssigned : byUser) {
                MasterRoot role = tree.getById(roleAssigned.getRoleId());
                if (role == null) {
                    // Loggear o manejar IDs de rol inconsistentes, saltar esta asignación
                    continue;
                }

                if (tree.isChildOf(role, "CONS")) {
                    // Si se encuentran múltiples roles de consumidor, se tomará el último.
                    // Si solo se espera un rol de consumidor, esto es suficiente.
                    List<String> permissionClient = permissionService.resolvePermissions(role);
                    clientResult = new ClientResult(
                            role.getFullName(),
                            true, // Asumiendo que true significa activo/disponible
                            permissionClient
                    );
                } else if (tree.isChildOf(role, "EMP")) {
                    // Si se encuentran múltiples roles de empleado, se tomará el último.
                    // Si solo se espera un rol de empleado, esto es suficiente.
                    List<String> permissionEmployee = permissionService.resolvePermissions(role);

                    // Solo crear OutletInfoDto si outletDomain NO es nulo
                    OutletInfoDto outletInfoDto = null;
                    if (outletDomain != null) {
                        outletInfoDto = new OutletInfoDto(outletDomain.getId(), outletDomain.getName());
                    }

                    employeeResult = new EmployeeResult(
                            true, // Asumiendo que true significa activo/disponible
                            outletInfoDto, // Puede ser null si no hay outletDomain
                            role,
                            permissionEmployee
                    );
                }
            }
        }

        return new ModesResult(clientResult, employeeResult);
    }

    @Override
    public ModesResponseDto mapperToResponse(ModesResult mode) {
        ClientModeDto client = null;
        EmployeeModeDto employee = null;
        if (mode.client() != null) {
            client = new ClientModeDto(
                    mode.client().type(),
                    mode.client().status(),
                    mode.client().permissions()
            );
        }
        if (mode.employee() != null) {
            OutletInfoDto outletDto = mode.employee().outlet(); // Esto puede ser null, lo cual es manejado por OutletInfoDto

            MasterDataResponseDto role = new MasterDataResponseDto(
                    mode.employee().role().getId(),
                    mode.employee().role().getShortName(),
                    mode.employee().role().getFullName()
            );

            employee = new EmployeeModeDto(
                    mode.employee().status(),
                    outletDto,
                    role,
                    mode.employee().permissions()
            );
        }

        return new ModesResponseDto(
                client,
                employee
        );
    }
}
package org.frias.avalon.domain.company.application.usecase.assign;

import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.domain.company.application.dto.request.UpdateCompanyEmployeeStatusRequest;
import org.frias.avalon.domain.company.application.dto.response.CompanyEmployeeResponse;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.outlet.domain.model.OutletDomain;
import org.frias.avalon.domain.outlet.domain.port.OutletRepositoryPort;
import org.frias.avalon.domain.person.domain.model.PersonDomain;
import org.frias.avalon.domain.person.domain.port.PersonRepositoryPort;
import org.frias.avalon.domain.user.domain.model.RoleAssignmentDomain;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.frias.avalon.domain.user.domain.port.RoleAssignmentRepositoryPort;
import org.frias.avalon.domain.user.domain.port.UserAvalonRepositoryPort;
import org.frias.avalon.core.jwt.service.SessionRevocationRegistry;
import org.frias.avalon.domain.user.application.dtos.response.SessionSyncMessage;
import org.frias.avalon.domain.user.presentation.UserSessionWebSocketPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ChangeCompanyEmployeeStatusUseCaseImpl implements ChangeCompanyEmployeeStatusUseCase {

    private final RoleAssignmentRepositoryPort roleAssignmentPort;
    private final OutletRepositoryPort outletPort;
    private final UserAvalonRepositoryPort userPort;
    private final PersonRepositoryPort personPort;
    private final MasterTreeProvider masterTreeProvider;
    private final SessionRevocationRegistry sessionRevocationRegistry;
    private final UserSessionWebSocketPublisher userSessionWebSocketPublisher;

    public ChangeCompanyEmployeeStatusUseCaseImpl(
            RoleAssignmentRepositoryPort roleAssignmentPort,
            OutletRepositoryPort outletPort,
            UserAvalonRepositoryPort userPort,
            PersonRepositoryPort personPort,
            MasterTreeProvider masterTreeProvider,
            SessionRevocationRegistry sessionRevocationRegistry,
            UserSessionWebSocketPublisher userSessionWebSocketPublisher
    ) {
        this.roleAssignmentPort = roleAssignmentPort;
        this.outletPort = outletPort;
        this.userPort = userPort;
        this.personPort = personPort;
        this.masterTreeProvider = masterTreeProvider;
        this.sessionRevocationRegistry = sessionRevocationRegistry;
        this.userSessionWebSocketPublisher = userSessionWebSocketPublisher;
    }

    @Override
    @Transactional
    public CompanyEmployeeResponse execute(Long companyId, Long userId, UpdateCompanyEmployeeStatusRequest request) {
        MasterTree tree = masterTreeProvider.getTree();

        // 1. Resolver el nuevo estado maestro por codigo semantico (ej. "ACT", "INA", "SUSP")
        MasterRoot newStatusNode = tree.getByCode(request.statusCode().trim());
        if (newStatusNode == null) {
            throw new ResourceNotFoundException("Estado maestro no encontrado con codigo: " + request.statusCode());
        }

        // 2. Localizar todas las tiendas de la empresa
        List<OutletDomain> companyOutlets = outletPort.findByCompanyId(companyId);
        Set<Long> companyOutletIds = companyOutlets.stream()
                .map(OutletDomain::getId)
                .collect(Collectors.toSet());

        // 3. Buscar la asignacion laboral especifica del usuario en esta empresa o sus tiendas
        List<RoleAssignmentDomain> userAssignments = roleAssignmentPort.findByUserAvalonId(userId);

        RoleAssignmentDomain targetAssignment = userAssignments.stream()
                .filter(a -> {
                    if (request.outletId() != null) {
                        return request.outletId().equals(a.getOutletId());
                    }
                    if (a.getCompanyId() != null && a.getCompanyId().equals(companyId)) {
                        return true;
                    }
                    return a.getOutletId() != null && companyOutletIds.contains(a.getOutletId());
                })
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Asignacion laboral no encontrada para el empleado en esta empresa"));

        // 4. Actualizar el estado de la asignacion laboral
        targetAssignment.changeStatus(newStatusNode.getId());
        roleAssignmentPort.update(targetAssignment);

        // 4.1 Revocar sesion previa del usuario para forzar reautenticacion / refresh
        sessionRevocationRegistry.revokeUser(userId);

        // 4.2 Notificar al cliente movil en tiempo real a traves del canal STOMP
        userSessionWebSocketPublisher.broadcastSessionSync(
                userId,
                new SessionSyncMessage(
                        "STATUS_CHANGE",
                        userId,
                        newStatusNode.getShortName(),
                        targetAssignment.getOutletId(),
                        System.currentTimeMillis()
                )
        );

        // 5. Enriquecer respuesta
        UserAvalonDomain user = userPort.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + userId));

        PersonDomain person = null;
        if (user.getPersonId() != null) {
            person = personPort.findById(user.getPersonId()).orElse(null);
        }

        MasterRoot roleRoot = tree.getById(targetAssignment.getRoleId());
        String roleCode = roleRoot != null ? roleRoot.getShortName() : "";
        String roleName = roleRoot != null ? roleRoot.getFullName() : "";

        String category = "OPERATIVO";
        if (roleRoot != null && roleRoot.getParentId() != null) {
            MasterRoot parent = tree.getById(roleRoot.getParentId());
            if (parent != null) {
                category = parent.getShortName();
            }
        }

        String outletName = "Sede Corporativa / Empresa";
        if (targetAssignment.getOutletId() != null) {
            outletName = companyOutlets.stream()
                    .filter(o -> o.getId().equals(targetAssignment.getOutletId()))
                    .map(OutletDomain::getName)
                    .findFirst()
                    .orElse("Tienda #" + targetAssignment.getOutletId());
        }

        String typeIdCode = "CC";
        if (person != null && person.getTypeIdentificationId() != null) {
            MasterRoot typeIdRoot = tree.getById(person.getTypeIdentificationId());
            if (typeIdRoot != null) {
                typeIdCode = typeIdRoot.getShortName();
            }
        }

        return new CompanyEmployeeResponse(
                user.getId(),
                user.getUserName(),
                person != null ? person.getId() : null,
                person != null ? person.getName() : "",
                person != null ? person.getLastName() : "",
                person != null ? person.getNumberid() : "",
                person != null ? person.getEmail() : null,
                person != null ? person.getPhoneNumber() : null,
                targetAssignment.getRoleId(),
                roleCode,
                roleName,
                category,
                targetAssignment.getOutletId(),
                outletName,
                newStatusNode.getId(),
                newStatusNode.getFullName(),
                typeIdCode
        );
    }
}

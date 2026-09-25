package org.frias.avalon.domain.company.application.usecase.assign;

import org.frias.avalon.core.exeptions.BusinessException;
import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.domain.company.application.dto.request.TransferCompanyEmployeeRequest;
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
public class TransferCompanyEmployeeUseCaseImpl implements TransferCompanyEmployeeUseCase {

    private final RoleAssignmentRepositoryPort roleAssignmentPort;
    private final OutletRepositoryPort outletPort;
    private final UserAvalonRepositoryPort userPort;
    private final PersonRepositoryPort personPort;
    private final MasterTreeProvider masterTreeProvider;
    private final SessionRevocationRegistry sessionRevocationRegistry;
    private final UserSessionWebSocketPublisher userSessionWebSocketPublisher;

    public TransferCompanyEmployeeUseCaseImpl(
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
    public CompanyEmployeeResponse execute(Long companyId, Long userId, TransferCompanyEmployeeRequest request) {
        MasterTree tree = masterTreeProvider.getTree();

        // 1. Validar que la tienda destino exista y pertenezca a la empresa
        OutletDomain targetOutlet = outletPort.findById(request.targetOutletId())
                .orElseThrow(() -> new ResourceNotFoundException("Tienda destino no encontrada con ID: " + request.targetOutletId()));

        if (!companyId.equals(targetOutlet.getCompanyId())) {
            throw new BusinessException("La tienda destino no pertenece a la misma empresa");
        }

        // 2. Obtener todas las tiendas de la empresa para ubicar la asignacion previa
        List<OutletDomain> companyOutlets = outletPort.findByCompanyId(companyId);
        Set<Long> companyOutletIds = companyOutlets.stream()
                .map(OutletDomain::getId)
                .collect(Collectors.toSet());

        // 3. Buscar asignacion laboral del usuario en la empresa
        List<RoleAssignmentDomain> userAssignments = roleAssignmentPort.findByUserAvalonId(userId);

        RoleAssignmentDomain targetAssignment = userAssignments.stream()
                .filter(a -> {
                    if (a.getCompanyId() != null && a.getCompanyId().equals(companyId)) {
                        return true;
                    }
                    return a.getOutletId() != null && companyOutletIds.contains(a.getOutletId());
                })
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Asignacion laboral no encontrada para el empleado en esta empresa"));

        // 4. Trasladar al empleado reasignando su outletId
        targetAssignment.changeOutlet(targetOutlet.getId());
        roleAssignmentPort.update(targetAssignment);

        // 4.1 Revocar sesion previa del usuario para forzar reautenticacion / refresh
        sessionRevocationRegistry.revokeUser(userId);

        // 4.2 Notificar al cliente movil en tiempo real a traves del canal STOMP
        userSessionWebSocketPublisher.broadcastSessionSync(
                userId,
                new SessionSyncMessage(
                        "TRANSFERRED",
                        userId,
                        "ACT",
                        targetOutlet.getId(),
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

        MasterRoot statusRoot = tree.getById(targetAssignment.getStatus());
        String statusName = statusRoot != null ? statusRoot.getFullName() : "ACTIVO";

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
                targetOutlet.getId(),
                targetOutlet.getName(),
                targetAssignment.getStatus(),
                statusName,
                typeIdCode
        );
    }
}

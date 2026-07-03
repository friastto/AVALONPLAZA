package org.frias.avalon.domain.user.application.usecase.changestatus;

import org.frias.avalon.core.exeptions.BusinessException;
import org.frias.avalon.core.permissions.CurrentUserProviderPort;
import org.frias.avalon.core.permissions.UserContext;
import org.frias.avalon.core.permissions.validchangestatus.StatusChangeValidator;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.model.StatusRules;
import org.frias.avalon.domain.masterdata.domain.repository.MasterDataRepositoryPort;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.user.application.dtos.request.ChangeUserAvalonStatusRequest;
import org.frias.avalon.domain.user.application.dtos.response.UserAvalonResponseDto;
import org.frias.avalon.domain.user.domain.mapper.UserAvalonMapper;
import org.frias.avalon.domain.user.domain.model.RoleAssignmentDomain;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.frias.avalon.domain.user.domain.port.RoleAssignmentRepositoryPort;
import org.frias.avalon.domain.user.domain.port.UserAvalonRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementación del caso de uso para cambiar el estado operativo de un usuario.
 * <p>
 * Orquesta la validación de autorización mediante el {@link StatusChangeValidator}, garantizando que un
 * empleado de una determinada tienda esté encapsulado en el entorno de su tienda y no pueda modificar
 * usuarios de otras tiendas.
 */
@Service
public class ChangeStatusUserAvalonUseCaseImpl implements ChangeStatusUserAvalonUseCase {

    private final UserAvalonRepositoryPort userAvalonPort;
    private final MasterDataRepositoryPort masterDataPort;
    private final StatusChangeValidator statusChangeValidator;
    private final UserAvalonMapper userAvalonMapper;
    private final RoleAssignmentRepositoryPort roleAssignmentPort;
    private final MasterTreeProvider masterTreeProvider;
    private final CurrentUserProviderPort currentUserProvider;

    public ChangeStatusUserAvalonUseCaseImpl(
            UserAvalonRepositoryPort userAvalonPort,
            MasterDataRepositoryPort masterDataPort,
            StatusChangeValidator statusChangeValidator,
            UserAvalonMapper userAvalonMapper,
            RoleAssignmentRepositoryPort roleAssignmentPort,
            MasterTreeProvider masterTreeProvider,
            CurrentUserProviderPort currentUserProvider
    ) {
        this.userAvalonPort = userAvalonPort;
        this.masterDataPort = masterDataPort;
        this.statusChangeValidator = statusChangeValidator;
        this.userAvalonMapper = userAvalonMapper;
        this.roleAssignmentPort = roleAssignmentPort;
        this.masterTreeProvider = masterTreeProvider;
        this.currentUserProvider = currentUserProvider;
    }

    /**
     * Ejecuta el cambio de estado de un usuario.
     *
     * @param request DTO con el ID del usuario objetivo y el código del nuevo estado maestro.
     * @return {@link UserAvalonResponseDto} con los datos actualizados del usuario.
     * @throws BusinessException si el usuario no existe, el estado no existe, la transición de estado
     *                           no está permitida, o el ejecutor no tiene permisos para realizar el cambio.
     */
    @Transactional
    @Override
    public UserAvalonResponseDto execute(ChangeUserAvalonStatusRequest request) {

        // --- 1. Obtener el usuario objetivo ---
        UserAvalonDomain targetUser = userAvalonPort.findById(request.userAvalonId())
                .orElseThrow(() -> new BusinessException(
                        "Usuario no encontrado con ID: " + request.userAvalonId()
                ));

        // --- 2. Obtener el estado actual y el nuevo estado ---
        MasterRoot oldStatus = masterDataPort.findById(targetUser.getStatusId())
                .orElseThrow(() -> new BusinessException(
                        "Estado actual del usuario no encontrado."
                ));

        MasterRoot newStatus = masterDataPort.findByCode(request.statusCode())
                .orElseThrow(() -> new BusinessException(
                        "Estado maestro no encontrado con código: " + request.statusCode()
                ));

        // --- 3. Validar la transición de estados ---
        StatusRules.validateTransition(oldStatus, newStatus);

        // --- 4. Construir el contexto del ejecutor desde el proveedor inyectado ---
        UserContext executorContext = currentUserProvider.getCurrentUserContext();

        // --- 5. Determinar si el cambio es sobre el propio usuario ---
        boolean isSelfChange = targetUser.getUserName()
                .equalsIgnoreCase(executorContext.username());

        // --- 6. Encapsular en el entorno de la tienda (Tenant/Outlet validation) ---
        boolean isSystemAdmin = currentUserProvider.hasRole("ROLE_ADMIN") || currentUserProvider.hasRole("ROLE_ADMINTI");
        String targetUserRoleCode = null;
        final Long tenantOutletId;

        if (!isSystemAdmin) {
            // El empleado no administrador debe estar encapsulado en su tienda
            Long outletId = currentUserProvider.getCurrentOutletId();
            if (outletId == null) {
                throw new BusinessException("No se detectó una tienda asociada en el contexto del empleado actual.");
            }
            tenantOutletId = outletId;

            // Buscar la asignación de rol del usuario objetivo en la tienda del ejecutor
            List<RoleAssignmentDomain> targetAssignments = roleAssignmentPort.findByUserAvalonId(targetUser.getId());
            
            RoleAssignmentDomain matchingAssignment = targetAssignments.stream()
                    .filter(assignment -> outletId.equals(assignment.getOutletId()))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException(
                            "Acceso denegado: El usuario objetivo no pertenece a su misma tienda."
                    ));

            // Obtener el rol del usuario objetivo en esa tienda
            MasterTree tree = masterTreeProvider.getTree();
            MasterRoot targetRole = tree.getById(matchingAssignment.getRoleId());
            if (targetRole != null) {
                targetUserRoleCode = targetRole.getShortName();
            }

            // Actualizar el estado de la asignación del rol para reflejarlo en la lista de personal de la tienda
            matchingAssignment.changeStatus(newStatus.getId());
            roleAssignmentPort.create(matchingAssignment);
        } else {
            // Un administrador global tiene acceso a cualquier tienda
            List<RoleAssignmentDomain> targetAssignments = roleAssignmentPort.findByUserAvalonId(targetUser.getId());
            if (!targetAssignments.isEmpty()) {
                MasterTree tree = masterTreeProvider.getTree();
                // Tomamos la tienda y rol de su primera asignación
                RoleAssignmentDomain firstAssignment = targetAssignments.get(0);
                tenantOutletId = firstAssignment.getOutletId();
                MasterRoot targetRole = tree.getById(firstAssignment.getRoleId());
                if (targetRole != null) {
                    targetUserRoleCode = targetRole.getShortName();
                }

                // Desactivar todas sus asignaciones en las tiendas
                for (RoleAssignmentDomain assignment : targetAssignments) {
                    assignment.changeStatus(newStatus.getId());
                    roleAssignmentPort.create(assignment);
                }
            } else {
                tenantOutletId = null;
            }
        }

        // --- 7. Delegar la autorización al validador ---
        boolean isAuthorized = statusChangeValidator.validate(
                executorContext,
                targetUserRoleCode,
                tenantOutletId,
                newStatus.getShortName(),
                null,
                isSelfChange
        );

        if (!isAuthorized) {
            throw new BusinessException(
                    "No tienes permisos para cambiar el estado de este usuario. " +
                    "Esta operación requiere rol de Gerente o Administrador."
            );
        }

        // --- 8. Delegar el cambio de estado al agregado de Dominio ---
        targetUser.changeStatus(newStatus.getId());

        // --- 9. Persistir el agregado actualizado ---
        UserAvalonDomain savedUser = userAvalonPort.save(targetUser);

        // --- 10. Construir y retornar el DTO de respuesta ---
        return userAvalonMapper.toResponse(savedUser, newStatus);
    }
}

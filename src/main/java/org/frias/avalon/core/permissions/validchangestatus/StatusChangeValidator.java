package org.frias.avalon.core.permissions.validchangestatus;

import org.frias.avalon.core.permissions.UserContext;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Validador principal para cambios de estado, utilizando el patrón Strategy.
 * Orquesta las diferentes estrategias de validación para determinar si un cambio de estado es permitido.
 * <p>
 * Es un bean de Spring singleton — el {@link MasterTreeProvider} que contiene el árbol de roles
 * vive en memoria durante toda la ejecución del servidor, por lo que no necesita re-inyectarse
 * en cada llamada al método {@link #validate}.
 */
@Component
public class StatusChangeValidator {

    private final List<StatusChangeValidationStrategy> strategies;
    private final MasterTreeProvider masterTreeProvider;

    /**
     * Constructor único para inyección por Spring.
     * Las estrategias se inicializan aquí en orden de evaluación:
     * primero las más restrictivas (OPT, autocambio) y luego las permisivas (Gerente, Admin).
     *
     * @param masterTreeProvider El proveedor del árbol de datos maestros en memoria.
     */
    public StatusChangeValidator(MasterTreeProvider masterTreeProvider) {
        this.masterTreeProvider = masterTreeProvider;
        this.strategies = List.of(
                new EmployeeSelfChangeValidationStrategy(),
                new OperatorCannotChangeOtherStatusStrategy(),
                new ManagerOrAdminChangeValidationStrategy(),
                new AvalonUserTargetValidationStrategy()
        );
    }

    /**
     * Valida si un cambio de estado es permitido basándose en las estrategias configuradas.
     *
     * @param currentUser         El contexto del usuario que intenta realizar el cambio.
     * @param targetUserRoleCode  El código de rol del usuario cuyo estado se intenta cambiar.
     * @param targetUserCompanyId El ID de compañía/outlet del usuario objetivo.
     * @param newStatusCode       El nuevo código de estado que se intenta aplicar.
     * @param newStatusType       El tipo del nuevo estado (puede ser null).
     * @param isSelfChange        Indica si el cambio es sobre el propio usuario ejecutor.
     * @return {@code true} si el cambio es válido según al menos una estrategia aplicable.
     * @throws IllegalStateException si no se encuentra ninguna estrategia aplicable para el contexto.
     */
    public boolean validate(
            UserContext currentUser,
            String targetUserRoleCode,
            Long targetUserCompanyId,
            String newStatusCode,
            String newStatusType,
            boolean isSelfChange
    ) {
        MasterTree masterTree = masterTreeProvider.getTree();

        List<StatusChangeValidationStrategy> applicableStrategies = strategies.stream()
                .filter(s -> s.isApplicable(currentUser, targetUserRoleCode, targetUserCompanyId, isSelfChange, masterTree))
                .toList();

        if (applicableStrategies.isEmpty()) {
            throw new IllegalStateException(
                    "No applicable status change validation strategy found for the given context."
            );
        }

        return applicableStrategies.stream()
                .anyMatch(s -> s.isValid(currentUser, targetUserRoleCode, targetUserCompanyId, newStatusCode, newStatusType, isSelfChange, masterTree));
    }
}

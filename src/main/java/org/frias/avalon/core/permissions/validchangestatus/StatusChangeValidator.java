package org.frias.avalon.core.permissions.validchangestatus;

import org.frias.avalon.core.permissions.UserContext;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Validador principal para cambios de estado, utilizando el patrón Strategy.
 * Orquesta las diferentes estrategias de validación para determinar si un cambio de estado es permitido.
 */
public class StatusChangeValidator {

    private final List<StatusChangeValidationStrategy> strategies;
    private final MasterTreeProvider masterTreeProvider; // Nueva dependencia

    /**
     * Constructor que inicializa el validador con una lista específica de estrategias y un MasterTreeProvider.
     *
     * @param strategies         La lista de estrategias de validación a utilizar.
     * @param masterTreeProvider El proveedor del árbol de datos maestros.
     */
    public StatusChangeValidator(List<StatusChangeValidationStrategy> strategies, MasterTreeProvider masterTreeProvider) {
        this.strategies = new ArrayList<>(strategies); // Copia defensiva
        this.masterTreeProvider = masterTreeProvider;
    }

    /**
     * Constructor por defecto que inicializa el validador con las estrategias predefinidas.
     * NOTA: Este constructor ahora requiere un MasterTreeProvider.
     *
     * @param masterTreeProvider El proveedor del árbol de datos maestros.
     */
    public StatusChangeValidator(MasterTreeProvider masterTreeProvider) {
        this.strategies = Arrays.asList(
                new EmployeeSelfChangeValidationStrategy(),
                new ManagerOrAdminChangeValidationStrategy(),
                new AvalonUserTargetValidationStrategy()
        );
        this.masterTreeProvider = masterTreeProvider;
    }

    /**
     * Valida si un cambio de estado es permitido basándose en las estrategias configuradas.
     *
     * @param currentUser         El contexto del usuario que intenta realizar el cambio.
     * @param targetUserRoleCode  El código de rol del usuario cuyo estado se intenta cambiar.
     * @param targetUserCompanyId El ID de la compañía del usuario cuyo estado se intenta cambiar.
     * @param newStatusCode       El nuevo código de estado que se intenta aplicar.
     * @param newStatusType       El tipo del nuevo estado (ej. "STSGEN", "USR_STS").
     * @param isSelfChange        Indica si el cambio es sobre el propio usuario.
     * @return true si el cambio es válido según al menos una estrategia aplicable, false en caso contrario.
     * @throws IllegalStateException si no se encuentra ninguna estrategia aplicable para la validación.
     */
    public boolean validate(UserContext currentUser, String targetUserRoleCode, Long targetUserCompanyId, String newStatusCode, String newStatusType, boolean isSelfChange) {
        MasterTree masterTree = masterTreeProvider.getTree(); // Obtener el MasterTree

        List<StatusChangeValidationStrategy> applicableStrategies = strategies.stream()
                .filter(s -> s.isApplicable(currentUser, targetUserRoleCode, targetUserCompanyId, isSelfChange, masterTree)) // Pasar masterTree
                .toList();

        if (applicableStrategies.isEmpty()) {
            throw new IllegalStateException("No applicable status change validation strategy found for the given context.");
        }

        return applicableStrategies.stream()
                .anyMatch(s -> s.isValid(currentUser, targetUserRoleCode, targetUserCompanyId, newStatusCode, newStatusType, isSelfChange, masterTree)); // Pasar masterTree
    }
}

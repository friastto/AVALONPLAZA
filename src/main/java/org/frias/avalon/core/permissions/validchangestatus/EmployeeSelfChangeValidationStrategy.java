package org.frias.avalon.core.permissions.validchangestatus;

import org.frias.avalon.core.permissions.UserContext;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;

/**
 * Estrategia de validación que prohíbe a un empleado con rol 'OPT' cambiarse su propio estado.
 */
public class EmployeeSelfChangeValidationStrategy implements StatusChangeValidationStrategy {

    private static final String OPERATOR_ROLE_CODE = "OPT"; // Código de la categoría de rol de operador

    @Override
    public boolean isValid(
            UserContext currentUser,
            String targetUserRoleCode,
            Long targetUserCompanyId,
            String newStatusCode,
            String newStatusType,
            boolean isSelfChange,
            MasterTree masterTree // Nuevo parámetro
    ) {
        // Si es un cambio propio y el usuario actual es un operador (según el MasterTree), no es válido.
        return !(isSelfChange && isUserAnOperator(currentUser, masterTree));
    }

    @Override
    public boolean isApplicable(
            UserContext currentUser,
            String targetUserRoleCode,
            Long targetUserCompanyId,
            boolean isSelfChange,
            MasterTree masterTree // Nuevo parámetro
    ) {
        // Esta estrategia es aplicable si el cambio es sobre el propio usuario
        // y el usuario actual tiene un rol de operador (según el MasterTree).
        return isSelfChange && isUserAnOperator(currentUser, masterTree);
    }

    /**
     * Verifica si el usuario actual tiene un rol que es un operador o un hijo de la categoría de operador.
     *
     * @param currentUser El contexto del usuario.
     * @param masterTree  El árbol de datos maestros.
     * @return true si el usuario es un operador, false en caso contrario.
     */
    private boolean isUserAnOperator(UserContext currentUser, MasterTree masterTree) {
        for (String role : currentUser.roles()) {
            // Asumiendo que los roles en UserContext tienen el prefijo "ROLE_"
            // y que MasterTree.isChildOf espera el código sin prefijo.
            String roleCode = role.startsWith("ROLE_") ? role.substring(5) : role;
            if (OPERATOR_ROLE_CODE.equals(roleCode) || masterTree.isChildOf(masterTree.getByCode(roleCode), OPERATOR_ROLE_CODE)) {
                return true;
            }
        }
        return false;
    }
}

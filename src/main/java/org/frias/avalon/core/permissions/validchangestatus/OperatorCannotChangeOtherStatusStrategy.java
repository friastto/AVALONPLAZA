package org.frias.avalon.core.permissions.validchangestatus;

import org.frias.avalon.core.permissions.UserContext;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;

/**
 * Estrategia de validación que impide que un empleado con rol operativo (OPT o sus hijos
 * como CJTURNO, CJPRINCIPAL) cambie el estado de otros usuarios.
 * <p>
 * Un operativo solo puede ejecutar sus tareas de caja e inventario; la gestión
 * de cuentas es una responsabilidad exclusiva del Gerente o Administrador.
 */
public class OperatorCannotChangeOtherStatusStrategy implements StatusChangeValidationStrategy {

    private static final String OPERATOR_CATEGORY_CODE = "OPT";

    /**
     * {@inheritDoc}
     * <p>
     * Esta estrategia siempre deniega el cambio cuando es aplicable (el ejecutor es OPT
     * y el objetivo es otro usuario), por lo que siempre retorna {@code false}.
     */
    @Override
    public boolean isValid(
            UserContext currentUser,
            String targetUserRoleCode,
            Long targetUserCompanyId,
            String newStatusCode,
            String newStatusType,
            boolean isSelfChange,
            MasterTree masterTree
    ) {
        // Un operativo NUNCA puede cambiar el estado de otro usuario.
        return false;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Aplica cuando: el cambio NO es propio (es sobre otro usuario) y el ejecutor
     * tiene un rol que cae bajo la categoría OPT en el MasterTree.
     */
    @Override
    public boolean isApplicable(
            UserContext currentUser,
            String targetUserRoleCode,
            Long targetUserCompanyId,
            boolean isSelfChange,
            MasterTree masterTree
    ) {
        // Solo aplica cuando el cambio es sobre otro usuario, no sobre sí mismo.
        if (isSelfChange) {
            return false;
        }
        return isUserAnOperator(currentUser, masterTree);
    }

    /**
     * Verifica si el usuario actual tiene un rol operativo (OPT o cualquier hijo de OPT en el MasterTree).
     *
     * @param currentUser El contexto del usuario ejecutor.
     * @param masterTree  El árbol de datos maestros para consultar la jerarquía de roles.
     * @return {@code true} si el usuario es un operativo, {@code false} en caso contrario.
     */
    private boolean isUserAnOperator(UserContext currentUser, MasterTree masterTree) {
        for (String role : currentUser.roles()) {
            String roleCode = role.startsWith("ROLE_") ? role.substring(5) : role;
            MasterRoot node = masterTree.getByCode(roleCode);
            if (node == null) continue;
            if (OPERATOR_CATEGORY_CODE.equalsIgnoreCase(roleCode)
                    || masterTree.isChildOf(node, OPERATOR_CATEGORY_CODE)) {
                return true;
            }
        }
        return false;
    }
}

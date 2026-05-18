package org.frias.avalon.core.permissions.validchangestatus;

import org.frias.avalon.core.permissions.UserContext;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree; // Importar MasterTree

/**
 * Estrategia de validación para cambios de estado donde el usuario objetivo es un usuario "AVALON".
 * Solo permitido si el usuario actual pertenece a "AVALON" y su rol es "SUPERADMIN".
 */
public class AvalonUserTargetValidationStrategy implements StatusChangeValidationStrategy {

    private static final String ADMIN_CATEGORY_CODE = "ADMIN"; // Asumiendo que SUPERADMIN cae bajo la categoría ADMIN

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
        // Solo un SUPERADMIN de AVALON puede cambiar el estado de un usuario AVALON.
        // Esto implica que el usuario actual debe ser un administrador (según el MasterTree)
        // Y el usuario actual debe pertenecer a AVALON (identificado por companyId == null).
        return isUserAnAdmin(currentUser, masterTree) && currentUser.employeeOutletId() == null;
    }

    @Override
    public boolean isApplicable(
            UserContext currentUser,
            String targetUserRoleCode,
            Long targetUserCompanyId,
            boolean isSelfChange,
            MasterTree masterTree // Nuevo parámetro
    ) {
        // Esta estrategia es aplicable si el usuario objetivo es un usuario AVALON (companyId == null).
        return targetUserCompanyId == null;
    }

    /**
     * Verifica si el usuario actual tiene un rol que es un administrador o un hijo de la categoría de administrador.
     * @param currentUser El contexto del usuario.
     * @param masterTree El árbol de datos maestros.
     * @return true si el usuario es un administrador, false en caso contrario.
     */
    private boolean isUserAnAdmin(UserContext currentUser, MasterTree masterTree) {
        for (String role : currentUser.roles()) {
            String roleCode = role.startsWith("ROLE_") ? role.substring(5) : role; // Eliminar prefijo ROLE_
            if (ADMIN_CATEGORY_CODE.equals(roleCode) || masterTree.isChildOf(masterTree.getByCode(roleCode), ADMIN_CATEGORY_CODE)) {
                return true;
            }
        }
        return false;
    }
}

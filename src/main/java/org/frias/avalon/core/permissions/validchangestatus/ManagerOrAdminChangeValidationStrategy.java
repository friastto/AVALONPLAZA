package org.frias.avalon.core.permissions.validchangestatus;

import org.frias.avalon.core.permissions.UserContext;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;

/**
 * Estrategia de validación para cambios de estado realizados por gerentes o administradores.
 * Permite a gerentes de la misma compañía o administradores de Avalon cambiar el estado de empleados subordinados.
 */
public class ManagerOrAdminChangeValidationStrategy implements StatusChangeValidationStrategy {

    private static final String GERENTE_CATEGORY_CODE = "GERENTE";
    private static final String ADMIN_CATEGORY_CODE = "ADMIN"; // Código de la categoría de rol de administrador

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
        // Un administrador (según el MasterTree) siempre puede realizar el cambio.
        if (isUserAnAdmin(currentUser, masterTree)) {
            return true;
        }

        // Un gerente (según el MasterTree) puede realizar el cambio si es de la misma compañía que el usuario objetivo.
        if (isUserAGerente(currentUser, masterTree) &&
                currentUser.employeeOutletId() != null) {
            // El gerente puede usar estados generales o específicos de usuario.
            return true;
        }

        return false;
    }

    @Override
    public boolean isApplicable(
            UserContext currentUser,
            String targetUserRoleCode,
            Long targetUserCompanyId,
            boolean isSelfChange,
            MasterTree masterTree // Nuevo parámetro
    ) {
        // Esta estrategia es aplicable si el usuario actual es un administrador o un gerente,
        // y no es un cambio propio (ya que eso lo maneja otra estrategia más específica).
        // También, no debe ser un usuario de AVALON como objetivo (eso lo maneja otra estrategia).

        boolean isCurrentUserAdminOrManager = isUserAnAdmin(currentUser, masterTree) || isUserAGerente(currentUser, masterTree);

        // Esta estrategia es para cambios *a otros* empleados por gerentes/admins.
        // Por lo tanto, no es aplicable para cambios propios.
        if (isSelfChange) {
            return false;
        }

        // Si el usuario objetivo es un usuario de AVALON, esta estrategia no es aplicable,
        // ya que hay una estrategia específica para eso.
        // Asumimos que un usuario de AVALON tiene companyId == null.
        if (targetUserCompanyId == null) {
            return false;
        }

        // Si el usuario actual es un administrador o gerente, esta estrategia es potencialmente aplicable.
        return isCurrentUserAdminOrManager;
    }

    /**
     * Verifica si el usuario actual tiene un rol que es un administrador o un hijo de la categoría de administrador.
     *
     * @param currentUser El contexto del usuario.
     * @param masterTree  El árbol de datos maestros.
     * @return true si el usuario es un administrador, false en caso contrario.
     */
    private boolean isUserAnAdmin(UserContext currentUser, MasterTree masterTree) {
        for (String role : currentUser.roles()) {
            String roleCode = role.startsWith("ROLE_") ? role.substring(5) : role;
            if (ADMIN_CATEGORY_CODE.equals(roleCode) || masterTree.isChildOf(masterTree.getByCode(roleCode), ADMIN_CATEGORY_CODE)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Verifica si el usuario actual tiene un rol que es un gerente o un hijo de la categoría de gerente.
     *
     * @param currentUser El contexto del usuario.
     * @param masterTree  El árbol de datos maestros.
     * @return true si el usuario es un gerente, false en caso contrario.
     */
    private boolean isUserAGerente(UserContext currentUser, MasterTree masterTree) {
        for (String role : currentUser.roles()) {
            String roleCode = role.startsWith("ROLE_") ? role.substring(5) : role;
            if (GERENTE_CATEGORY_CODE.equals(roleCode) || masterTree.isChildOf(masterTree.getByCode(roleCode), GERENTE_CATEGORY_CODE)) {
                return true;
            }
        }
        return false;
    }
}

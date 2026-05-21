package org.frias.avalon.core.permissions.validchangestatus;

import org.frias.avalon.core.permissions.UserContext;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;

/**
 * Interfaz para las estrategias de validación de cambio de estado.
 * Cada implementación contendrá una lógica de validación específica.
 */
public interface StatusChangeValidationStrategy {

    /**
     * Valida si un cambio de estado es permitido según la estrategia implementada.
     *
     * @param currentUser         El contexto del usuario que intenta realizar el cambio.
     * @param targetUserRoleCode  El código de rol del usuario cuyo estado se intenta cambiar.
     * @param targetUserCompanyId El ID de la compañía del usuario cuyo estado se intenta cambiar.
     * @param newStatusCode       El nuevo código de estado que se intenta aplicar.
     * @param newStatusType       El tipo del nuevo estado (ej. "STSGEN", "USR_STS").
     * @param isSelfChange        Indica si el cambio es sobre el propio usuario.
     * @param masterTree          El árbol de datos maestros para consultar jerarquías de roles.
     * @return true si el cambio es válido según esta estrategia, false en caso contrario.
     */
    boolean isValid(
            UserContext currentUser,
            String targetUserRoleCode,
            Long targetUserCompanyId,
            String newStatusCode,
            String newStatusType,
            boolean isSelfChange,
            MasterTree masterTree
    );

    /**
     * Determina si esta estrategia es aplicable al escenario actual.
     *
     * @param currentUser         El contexto del usuario que intenta realizar el cambio.
     * @param targetUserRoleCode  El código de rol del usuario cuyo estado se intenta cambiar.
     * @param targetUserCompanyId El ID de la compañía del usuario cuyo estado se intenta cambiar.
     * @param isSelfChange        Indica si el cambio es sobre el propio usuario.
     * @param masterTree          El árbol de datos maestros para consultar jerarquías de roles.
     * @return true si la estrategia debe ser considerada para la validación, false en caso contrario.
     */
    boolean isApplicable(
            UserContext currentUser,
            String targetUserRoleCode,
            Long targetUserCompanyId,
            boolean isSelfChange,
            MasterTree masterTree
    );
}

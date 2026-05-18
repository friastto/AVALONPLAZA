package org.frias.avalon.core.permissions;

import java.util.List;

/**
 * Representa el contexto del usuario actual que realiza una operación.
 * Contiene la información necesaria para las validaciones de permisos.
 * Con la nueva estrategia, las categorizaciones de roles se realizarán
 * utilizando el MasterTree en las propias estrategias.
 */
public record UserContext(
        String username,
        List<String> roles, // Roles con prefijo ROLE_ (ej. ROLE_ADMIN, ROLE_GERGEN)
        Long employeeOutletId   // OutletId asociado al empleado (si aplica)
        //Long companyId           // CompanyId asociado al tenant (null para usuarios AVALON)
) {
}
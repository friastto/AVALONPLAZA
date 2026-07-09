package org.frias.avalon.domain.user.application.service;

import org.frias.avalon.core.exeptions.BusinessException;
import org.frias.avalon.core.jwt.util.SecurityUtils;
import org.frias.avalon.core.tenant.TenantContext;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class PermissionService {
    private final MasterTreeProvider treeProvider;

    public PermissionService(MasterTreeProvider treeProvider) {
        this.treeProvider = treeProvider;
    }

    public List<String> resolvePermissions(List<MasterRoot> roles) {

        Set<String> permissions = new HashSet<>();

        for (MasterRoot role : roles) {

            permissions.addAll(permisos(role));
        }

        return new ArrayList<>(permissions);
    }

    public List<String> resolvePermissions(MasterRoot role) {

        return new ArrayList<>(permisos(role));
    }

    private Set<String> permisos(MasterRoot role) {
        Set<String> permissions = new HashSet<>();
        switch (role.getShortName()) {
            case "ADMIN":
            case "ADMINTI":
                permissions.add("FULL_ADMIN_ACCESS");
                //permissions.add("ASSIGN_ANY_ROLE"); // Nuevo permiso para ADMIN/ADMINTI
                break;

            case "GERGEN":
                permissions.add("VIEW_DASHBOARD");
                permissions.add("MANAGE_EMPLOYEE");
                permissions.add("POS_SALES");
                permissions.add("MANAGE_INVENTORY");
                permissions.add("ASSIGN_OPERATIVE_ROLE");
                
                // Nuevos permisos enriquecidos
                permissions.add("MANAGE_USERS");          // Crear, editar, suspender y eliminar cuentas de empleados
                permissions.add("CRITICAL_INVENTORY");     // Ajustar stock a mano, cambiar precios y márgenes
                permissions.add("FINANCE_REPORTS");        // Ver reportes de ventas, ganancias, gastos
                permissions.add("MANAGE_SUPPLIERS");       // Registrar proveedores, órdenes de compra y facturas
                permissions.add("SYSTEM_CONFIG");          // Datos de factura, impresoras, categorías, básculas
                permissions.add("STORE_OPERATIONS");       // Apertura/cierre local, arqueos y recepción de proveedores
                permissions.add("CASHIER_AUTHORIZATION");   // Autorización de descuentos o cancelaciones en caja
                
                // El gerente también tiene acceso a las tareas del cajero
                permissions.add("RECEIVE_INVENTORY");      // Recepción de mercancía de proveedores
                permissions.add("QUERY_PRODUCTS");         // Consultar precios, stock y alertas
                permissions.add("EXPIRY_CONTROL");         // Control de fechas de caducidad
                break;
 
            case "CJTURNO":
            case "CJPRINCIPAL": // CAJERO_PRINCIPAL (del masterdata real de la base de datos)
            case "CAJPRIN":   // CAJERO_PRINCIPAL (árbol actualizado)
            case "CAJTUR":    // CAJERO_TURNO (árbol actualizado)
                permissions.add("POS_SALES");              // Apertura/cierre turno, escanear, cobrar, emitir ticket
                permissions.add("RECEIVE_INVENTORY");      // Recepción de mercancía
                permissions.add("QUERY_PRODUCTS");         // Consultar precios y stock
                permissions.add("EXPIRY_CONTROL");         // Control de vencimientos
                break;

            case "CSTNDR":
                permissions.add("VIEW_MARKETPLACE");
                permissions.add("BUY_PRODUCTS");
                permissions.add("AUTO_ASSIGN_CONSUMER_ROLE"); // CSTNDR también pued
                break;

            case "USANONIMO":
                permissions.add("VIEW_MARKETPLACE");
                permissions.add("AUTO_ASSIGN_CONSUMER_ROLE"); // Nuevo permiso para ANONIMO
                permissions.add("AUTO_ASSIGN_CONSUMER_ROLE"); // CSTNDR también pued
                break;
        }

        return permissions;
    }


    /**
     * Verifica si el usuario actual tiene permiso para asignar un rol específico.
     * Esta es la lógica central de autorización para la asignación de roles.
     *
     * @param roleToAssignMasterRoot El MasterRoot del rol que se intenta asignar.
     * @param requestOutletId        El ID del outlet especificado en la solicitud de asignación.
     * @return true si el usuario actual está autorizado, false en caso contrario.
     */
    public boolean canAssignRole(MasterRoot roleToAssignMasterRoot, Long requestOutletId) {
        MasterTree tree = treeProvider.getTree();

        // 1. Obtener información del usuario que realiza la solicitud
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        String currentEmployeeRoleCode = TenantContext.getTenantRolEmployee();
        String currentConsumerRoleCode = TenantContext.getTenantRolConsumer();
        Long currentEmployeeOutletId = TenantContext.getTenantOutletId();

        // --- Escenario 1: Empleados Globales (ADMIN, ADMINTI) ---
        if (SecurityUtils.hasRole("ROLE_ADMIN") || SecurityUtils.hasRole("ROLE_ADMINTI")) {
            // Un ADMIN o ADMINTI puede asignar cualquier rol.
            // Podríamos añadir más granularidad aquí si un ADMIN no puede asignar ciertos roles.
            return true; // Tienen permiso total para asignar roles
        }

        // --- Escenario 2: Empleado de Outlet (GERGEN) ---
        if (SecurityUtils.hasRole("ROLE_GERGEN")) {
            // Un GERGEN solo puede asignar roles operativos (OPT) dentro de su propio outlet.
            if (currentEmployeeOutletId == null) {
                throw new BusinessException("El GERGEN debe estar asociado a un outlet para asignar roles.");
            }
            if (requestOutletId == null || !requestOutletId.equals(currentEmployeeOutletId)) {
                throw new BusinessException("Un GERGEN solo puede asignar roles dentro de su propio outlet.");
            }
            if (!tree.isChildOf(roleToAssignMasterRoot, "OPT")) {
                throw new BusinessException("Un GERGEN solo puede asignar roles operativos.");
            }
            return true; // Autorizado
        }

        // --- Escenario 3: Usuario Anónimo/Consumidor (Auto-asignación de rol de consumidor estándar) ---
        // Este escenario es un poco diferente, ya que el usuario se asigna a sí mismo.
        // La lógica de auto-asignación se manejará en el UseCase, pero aquí podemos validar el permiso.
        if (SecurityUtils.hasRole("ROLE_USANONIMO") || SecurityUtils.hasRole("ROLE_CSTNDR")) { // Asumiendo CSTNDR es el rol de consumidor estándar
            // Un usuario anónimo/consumidor solo puede auto-asignarse un rol de consumidor estándar
            // y solo a sí mismo (validación de userId en el UseCase)
            if (tree.isChildOf(roleToAssignMasterRoot, "CLIENTE") && requestOutletId == null) {
                // Podrías añadir una validación para que sea un rol de cliente "estándar" específico si lo deseas
                return true; // Autorizado para auto-asignarse un rol de cliente
            }
        }

        // --- Escenario 4: Otros roles (ej. OPT) ---
        // Por defecto, ningún otro rol tiene permiso para asignar roles.
        return false;
    }

    /**
     * Verifica si el usuario actual tiene permiso para auto-asignarse un rol de consumidor.
     * Este método es más específico para el escenario de auto-registro.
     * <p>
     * //* @param roleToAssignMasterRoot El MasterRoot del rol que se intenta auto-asignar.
     *
     * @return true si el usuario actual está autorizado a auto-asignarse este rol, false en caso contrario.
     */
    public boolean canAutoAssignConsumerRole(MasterRoot roleToAssignMasterRoot) {
        MasterTree tree = treeProvider.getTree();
        // Un usuario anónimo solo puede auto-asignarse un rol de consumidor estándar
        // y solo si el rol a asignar es un descendiente de CLIENTE
        // Un usuario ANÓNIMO o CONSUMIDOR (CSTNDR) puede auto-asignarse el rol CSTNDR
        boolean isAnonimoOrCstndr = SecurityUtils.hasRole("ROLE_ANONIMO") || SecurityUtils.hasRole("ROLE_CSTNDR");

        // El rol a auto-asignar debe ser específicamente CSTNDR y ser un descendiente de CLIENTE
        boolean isCstndrRole = roleToAssignMasterRoot.getShortName().equals("CSTNDR") && tree.isChildOf(roleToAssignMasterRoot, "CLIENTE");

        return isAnonimoOrCstndr && isCstndrRole;
    }

}

/**
 * cuando  coloque en el arbol de permisos enmasterdata de permisos por rol
 */
    /*
    @Component
public class PermissionService {

    private final MasterTreeProvider treeProvider;

    public PermissionService(MasterTreeProvider treeProvider) {
        this.treeProvider = treeProvider;
    }

    public List<String> resolvePermissions(List<MasterRoot> roles) {

        var tree = treeProvider.getTree();

        return roles.stream()
                .flatMap(role -> tree.getChildren(role).stream())
                .map(MasterRoot::getShortName)
                .distinct()
                .toList();
    }
}
     */

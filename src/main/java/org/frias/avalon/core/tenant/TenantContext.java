package org.frias.avalon.core.tenant;

import java.util.Collections;
import java.util.List;

public class TenantContext {

    private static final ThreadLocal<Long> currentTenant = new ThreadLocal<>();
    private static final ThreadLocal<Long> currentTenantOutlet = new ThreadLocal<>();
    private static final ThreadLocal<String> currentTenantRolEmployee = new ThreadLocal<>();
    private static final ThreadLocal<String> currentTenantRolConsumer = new ThreadLocal<>();



    public static Long getTenantId() {
        return currentTenant.get();
    }

    public static void setTenantId(Long tenantId) {
        currentTenant.set(tenantId);
    }

    public static Long getTenantOutletId() {
        return currentTenantOutlet.get();
    }

    public static void setTenantOutletId(Long tenantId) {
        currentTenantOutlet.set(tenantId);
    }

    public static String getTenantRolEmployee() {
        return currentTenantRolEmployee.get();
    }
    public static String getTenantRolConsumer() {
        return currentTenantRolConsumer.get();
    }

    public static void setTenantRolEmployee(String rolEmployee) {
        currentTenantRolEmployee.set(rolEmployee); // CORREGIDO: Ahora establece el rol de empleado
    }
    public static void setTenantRolConsumer(String rolConsumer) { // RENOMBRADO para consistencia
        currentTenantRolConsumer.set(rolConsumer);
    }


    public static void clear() {
        currentTenant.remove();
        currentTenantOutlet.remove();
        currentTenantRolConsumer.remove();
        currentTenantRolEmployee.remove();
    }
}

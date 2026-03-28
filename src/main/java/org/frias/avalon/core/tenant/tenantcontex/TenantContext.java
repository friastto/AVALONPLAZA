package org.frias.avalon.core.tenant.tenantcontex;

public class TenantContext {

    private static final ThreadLocal<Long> currentTenant = new ThreadLocal<>();
    private static final ThreadLocal<Long> currentTenantOutlet = new ThreadLocal<>();
    private static final ThreadLocal<String> currentTenantRol = new ThreadLocal<>();



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

    public static String getTenantRol() {
        return currentTenantRol.get();
    }

    public static void setTenantRol(String rol) {
        currentTenantRol.set(rol);
    }



    public static void clear() {
        currentTenant.remove();
        currentTenantOutlet.remove();
        currentTenantRol.remove();
    }



}

package org.frias.avalon.domain.company.facade;

import org.frias.avalon.core.tenant.tenantcontex.TenantContext;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class TenantSecurity {
    
    protected Long getCompanyId() { return TenantContext.getTenantId(); }
    protected Long getOutletId() { return TenantContext.getTenantOutletId(); }
    protected String getRol() { return TenantContext.getTenantRol(); }

    // Grupos de Roles (Listas únicas y finales)
    protected final Set<String> ROLES_AVALON = Set.of("ADMIN", "ADMINTI", "ADMINBD","ADMINSEGTI", "DIREC", "FUN", "DIROP");
    protected final Set<String> ROLES_COMPANY = Set.of("GERGEN", "GERTUR", "GERPI", "GERINVAL");
    protected final Set<String> ROLES_OUTLET = Set.of("SUPERVISOR", "CJPRINCIPAL", "CJTURNO", "VNDPISO");
    protected final Set<String> ROLES_CLIENTE = Set.of("CSTNDR", "CFREC", "CVIP", "CPREM");

    // 1. ¿Es Staff Maestro de Avalon?
    public boolean isMasterStaff() {

        return getCompanyId() == null && getOutletId() == null && ROLES_AVALON.contains(getRol());
    }

    // 2. Validar Company (Para GERGEN y superiores)
    public Long getValidatedCompanyId() {

        Long id = getCompanyId();

        // Si no tiene empresa y no es de los roles de Avalon, bloqueamos
        if (id == null && ROLES_AVALON.contains(getRol())) {
            throw new SecurityException("Operación no permitida: El usuario no tiene una empresa asignada.");
        }
        return id;
    }

    // 3. Validar Outlet (Para roles operativos de tienda)
    public Long getValidatedOutletId() {

        Long id = getOutletId();

        // Si no tiene Outlet y el rol pertenece al grupo de Outlets, es un error de datos
        if (id == null  && ROLES_OUTLET.contains(getRol())) {
            throw new SecurityException("Error : El usuario " + getRol() + " no esta vinculado a una sucursal.");
        }
        return id;
    }


}
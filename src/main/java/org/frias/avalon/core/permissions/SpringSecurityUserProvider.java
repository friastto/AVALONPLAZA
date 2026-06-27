package org.frias.avalon.core.permissions;

import org.frias.avalon.core.jwt.util.SecurityUtils;
import org.frias.avalon.core.tenant.TenantContext;
import org.springframework.stereotype.Component;

/**
 * Adaptador de infraestructura que implementa {@link CurrentUserProviderPort}.
 * Resuelve los datos de seguridad y tenant consumiendo directamente los mecanismos
 * de Spring Security y el contexto de hilos locales (ThreadLocal) de la API.
 */
@Component
public class SpringSecurityUserProvider implements CurrentUserProviderPort {

    @Override
    public UserContext getCurrentUserContext() {
        return SecurityUtils.getCurrentUserContext();
    }

    @Override
    public Long getCurrentOutletId() {
        return TenantContext.getTenantOutletId();
    }

    @Override
    public Long getCurrentTenantId() {
        return TenantContext.getTenantId();
    }

    @Override
    public boolean hasRole(String role) {
        return SecurityUtils.hasRole(role);
    }
}

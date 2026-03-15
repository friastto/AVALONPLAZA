package org.frias.avalon.empresasucursal.tenant.config;

//@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "empresaId", type = Long.class))
//@Filter(name = "tenantFilter", condition = "empresa_id = :empresaId")

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.frias.avalon.empresasucursal.tenant.tenantcontex.TenantContext;
import org.frias.avalon.jwt.util.SecurityUtils;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TenantAspect {

    @PersistenceContext
    private EntityManager entityManager;

    // ✅ Esta expresión sí funciona con proxies de Spring Data
    @Around("@within(org.frias.avalon.empresasucursal.tenant.config.TenantAware)")
    public Object activateFilter(ProceedingJoinPoint pjp) throws Throwable {

        System.out.println("procesando aspectos - método: " + pjp.getSignature().getName());

        // SuperAdmin ve todo, no filtra
        if (SecurityUtils.hasRole("ROLE_ADMINTI")) {
            return pjp.proceed();
        }

        Long empresaId = TenantContext.getTenantId();

        System.out.println("empresaId en contexto: " + empresaId);

        if (empresaId == null) {
            return pjp.proceed(); // o lanzar excepción según tu lógica
        }


        Session session = entityManager.unwrap(Session.class);

        // ✅ Activa ANTES, desactiva SIEMPRE en finally
        session.enableFilter("tenantFilter")
                .setParameter("empresaId", empresaId);
        try {
            return pjp.proceed();
        } finally {
            // 🔥 CRÍTICO: sin esto el filtro queda activo en el mismo session
            session.disableFilter("tenantFilter");
        }
    }
}
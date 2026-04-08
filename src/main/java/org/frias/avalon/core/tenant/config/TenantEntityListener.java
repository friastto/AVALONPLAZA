package org.frias.avalon.core.tenant.config;


import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import org.frias.avalon.core.tenant.tenantcontex.TenantContext;
import org.frias.avalon.domain.outlet.entities.Outlet;

public class TenantEntityListener {

    @PrePersist
    @PreUpdate
    @PostLoad
    public void syncTenant(Object entity) {



        if (!(entity instanceof TenantEntity te)) {
            return;
        }

        // 1️⃣ Prioridad: relación real (FK)
        if (entity instanceof Outlet o && o.getCompany() != null) {
            te.setEmpresaId(o.getCompany().getId());
            return;
        }

        // 2️⃣ Fallback: TenantContext
        Long tenantId = TenantContext.getTenantId();
        if (tenantId != null) {
            te.setEmpresaId(tenantId);
        }



    }
}

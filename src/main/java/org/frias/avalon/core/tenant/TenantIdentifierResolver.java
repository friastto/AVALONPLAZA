package org.frias.avalon.core.tenant;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;

@Component
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver {

    private static final String DEFAULT_TENANT = "public";

    @Override
    public String resolveCurrentTenantIdentifier() {
        Long companyId = TenantContext.getTenantId();
        if (companyId != null && companyId > 0) {
            return "company_" + companyId;
        }
        Long tenantOutletId = TenantContext.getTenantOutletId();
        if (tenantOutletId != null && tenantOutletId > 0) {
            return "store_" + tenantOutletId;
        }
        return DEFAULT_TENANT;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}

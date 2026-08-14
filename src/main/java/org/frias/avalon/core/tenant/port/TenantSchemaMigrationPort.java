package org.frias.avalon.core.tenant.port;

/**
 * Output Port for executing multi-tenant PostgreSQL schema migrations.
 */
public interface TenantSchemaMigrationPort {

    /**
     * Provisions or updates multi-tenant schema via Flyway.
     *
     * @param schemaName target tenant schema name (e.g. company_1)
     */
    void migrateTenantSchema(String schemaName);
}

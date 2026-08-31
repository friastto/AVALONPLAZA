package org.frias.avalon.core.tenant;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.frias.avalon.core.tenant.port.TenantSchemaMigrationPort;

@Service
public class FlywayMultiTenantService implements TenantSchemaMigrationPort {

    private final DataSource dataSource;

    @Value("${spring.flyway.locations.global:classpath:db/migration/global}")
    private String globalLocations;

    @Value("${spring.flyway.locations.tenant:classpath:db/migration/tenant}")
    private String tenantLocations;

    public FlywayMultiTenantService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void migrateGlobalSchema() {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .schemas("public")
                .locations(globalLocations)
                .baselineOnMigrate(true)
                .load();
        flyway.repair();
        flyway.migrate();
    }

    public void migrateTenantSchema(String schemaName) {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .schemas(schemaName, "public")
                .locations(tenantLocations)
                .baselineOnMigrate(true)
                .load();
        flyway.repair();
        flyway.migrate();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void migrateAllTenants() {
        migrateGlobalSchema();
        List<String> activeSchemas = getActiveOutletAndCompanySchemas();
        for (String schema : activeSchemas) {
            migrateTenantSchema(schema);
        }
    }

    private List<String> getActiveOutletAndCompanySchemas() {
        List<String> schemas = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {

            // 1. Discover schemas from existing outlets in public.outlet
            try (ResultSet rs = statement.executeQuery("SELECT id, company_id FROM public.outlet")) {
                while (rs.next()) {
                    long outletId = rs.getLong("id");
                    schemas.add("store_" + outletId);
                    long companyId = rs.getLong("company_id");
                    if (companyId > 0 && !schemas.contains("company_" + companyId)) {
                        schemas.add("company_" + companyId);
                    }
                }
            } catch (SQLException ignored) {
                // Table public.outlet might not exist yet during first initialization
            }

            // 2. Discover any other existing tenant schemas already in PostgreSQL
            try (ResultSet rs = statement.executeQuery(
                    "SELECT schema_name FROM information_schema.schemata WHERE schema_name LIKE 'company_%' OR schema_name LIKE 'store_%'")) {
                while (rs.next()) {
                    String schemaName = rs.getString("schema_name");
                    if (!schemas.contains(schemaName)) {
                        schemas.add(schemaName);
                    }
                }
            } catch (SQLException ignored) {
            }

        } catch (SQLException e) {
            // Log or ignore if connection error
        }
        return schemas;
    }
}

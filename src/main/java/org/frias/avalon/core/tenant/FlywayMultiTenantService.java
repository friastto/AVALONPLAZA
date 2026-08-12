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

@Service
public class FlywayMultiTenantService {

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
        List<String> storeSchemas = getAllStoreSchemas();
        for (String schema : storeSchemas) {
            migrateTenantSchema(schema);
        }
    }

    private List<String> getAllStoreSchemas() {
        List<String> schemas = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT schema_name FROM information_schema.schemata WHERE schema_name LIKE 'company_%' OR schema_name LIKE 'store_%'")) {
            while (resultSet.next()) {
                schemas.add(resultSet.getString("schema_name"));
            }
        } catch (SQLException e) {
            // Log or ignore if table/schemata not available yet
        }
        return schemas;
    }
}

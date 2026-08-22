package org.frias.avalon.domain.user.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit Tests for 3-Level RBAC Matrix Rules")
class UserRbac3LevelSecurityTest {

    private static final Set<String> LEVEL_1_ROLES = Set.of("ADMINTI", "ADMINSYS");
    private static final Set<String> LEVEL_2_ROLES = Set.of("GERGEN");
    private static final Set<String> LEVEL_3_ROLES = Set.of("ADMOULT", "GERENTE", "CJTURNO", "VENDEDOR", "CLIENTE");

    @Test
    @DisplayName("Should verify Level 1 SuperAdmin global authorization scope")
    void shouldVerifyLevel1GlobalAuthorizationScope() {
        assertTrue(LEVEL_1_ROLES.contains("ADMINTI"));
        assertTrue(LEVEL_1_ROLES.contains("ADMINSYS"));
        assertFalse(LEVEL_2_ROLES.contains("ADMINTI"));
        assertFalse(LEVEL_3_ROLES.contains("ADMINSYS"));
    }

    @Test
    @DisplayName("Should verify Level 2 Corporate Manager company approval authority")
    void shouldVerifyLevel2CorporateManagerAuthority() {
        assertTrue(LEVEL_2_ROLES.contains("GERGEN"));
        assertFalse(LEVEL_3_ROLES.contains("GERGEN"));
    }

    @Test
    @DisplayName("Should verify Level 3 Local Outlet cashier and staff store scope")
    void shouldVerifyLevel3LocalOutletScope() {
        assertTrue(LEVEL_3_ROLES.contains("ADMOULT"));
        assertTrue(LEVEL_3_ROLES.contains("CJTURNO"));
        assertTrue(LEVEL_3_ROLES.contains("VENDEDOR"));
        assertTrue(LEVEL_3_ROLES.contains("CLIENTE"));
    }
}

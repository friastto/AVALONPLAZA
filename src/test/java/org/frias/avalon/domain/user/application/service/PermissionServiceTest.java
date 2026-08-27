package org.frias.avalon.domain.user.application.service;

import org.frias.avalon.core.exeptions.BusinessException;
import org.frias.avalon.core.tenant.TenantContext;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests for PermissionService")
class PermissionServiceTest {

    @Mock
    private MasterTreeProvider treeProvider;

    @Mock
    private MasterTree masterTree;

    @InjectMocks
    private PermissionService permissionService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        TenantContext.clear();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        TenantContext.clear();
    }

    private void setupSecurityContext(String username, String... roles) {
        List<SimpleGrantedAuthority> authorities = java.util.Arrays.stream(roles)
                .map(SimpleGrantedAuthority::new)
                .toList();
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(username, "password", authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    // ==========================================
    // Tests for resolvePermissions(MasterRoot)
    // ==========================================

    @Test
    @DisplayName("Should resolve permissions for ADMIN role")
    void shouldResolvePermissionsForAdmin() {
        MasterRoot adminRole = new MasterRoot(1L, "ADMIN", "Administrador Global", null, 1L);
        List<String> permissions = permissionService.resolvePermissions(adminRole);

        assertTrue(permissions.contains("FULL_ADMIN_ACCESS"));
        assertEquals(1, permissions.size());
    }

    @Test
    @DisplayName("Should resolve permissions for ADMINTI role")
    void shouldResolvePermissionsForAdminTi() {
        MasterRoot adminTiRole = new MasterRoot(2L, "ADMINTI", "Administrador TI", null, 1L);
        List<String> permissions = permissionService.resolvePermissions(adminTiRole);

        assertTrue(permissions.contains("FULL_ADMIN_ACCESS"));
        assertEquals(1, permissions.size());
    }

    @Test
    @DisplayName("Should resolve permissions for GERGEN role")
    void shouldResolvePermissionsForGergen() {
        MasterRoot gergenRole = new MasterRoot(3L, "GERGEN", "Gerente General", null, 1L);
        List<String> permissions = permissionService.resolvePermissions(gergenRole);

        assertTrue(permissions.contains("VIEW_DASHBOARD"));
        assertTrue(permissions.contains("MANAGE_EMPLOYEE"));
        assertTrue(permissions.contains("POS_SALES"));
        assertTrue(permissions.contains("MANAGE_INVENTORY"));
        assertTrue(permissions.contains("ASSIGN_OPERATIVE_ROLE"));
        assertTrue(permissions.contains("MANAGE_USERS"));
        assertTrue(permissions.contains("CRITICAL_INVENTORY"));
        assertTrue(permissions.contains("FINANCE_REPORTS"));
        assertTrue(permissions.contains("MANAGE_SUPPLIERS"));
        assertTrue(permissions.contains("SYSTEM_CONFIG"));
        assertTrue(permissions.contains("STORE_OPERATIONS"));
        assertTrue(permissions.contains("CASHIER_AUTHORIZATION"));
        assertTrue(permissions.contains("RECEIVE_INVENTORY"));
        assertTrue(permissions.contains("QUERY_PRODUCTS"));
        assertTrue(permissions.contains("EXPIRY_CONTROL"));
        assertEquals(15, permissions.size());
    }

    @Test
    @DisplayName("Should resolve permissions for cashier roles (CJTURNO, CJPRINCIPAL, CAJPRIN, CAJTUR)")
    void shouldResolvePermissionsForCashierRoles() {
        List<String> cashierCodes = List.of("CJTURNO", "CJPRINCIPAL", "CAJPRIN", "CAJTUR");

        for (String code : cashierCodes) {
            MasterRoot cashierRole = new MasterRoot(4L, code, "Cajero", null, 1L);
            List<String> permissions = permissionService.resolvePermissions(cashierRole);

            assertEquals(3, permissions.size(), "Failed for code: " + code);
            assertTrue(permissions.contains("POS_SALES"));
            assertTrue(permissions.contains("QUERY_PRODUCTS"));
            assertTrue(permissions.contains("EXPIRY_CONTROL"));
        }
    }

    @Test
    @DisplayName("Should resolve permissions for CSTNDR role")
    void shouldResolvePermissionsForCstndr() {
        MasterRoot cstndrRole = new MasterRoot(5L, "CSTNDR", "Cliente Estándar", null, 1L);
        List<String> permissions = permissionService.resolvePermissions(cstndrRole);

        assertEquals(3, permissions.size());
        assertTrue(permissions.contains("VIEW_MARKETPLACE"));
        assertTrue(permissions.contains("BUY_PRODUCTS"));
        assertTrue(permissions.contains("AUTO_ASSIGN_CONSUMER_ROLE"));
    }

    @Test
    @DisplayName("Should resolve permissions for USANONIMO role")
    void shouldResolvePermissionsForUsAnonimo() {
        MasterRoot anonRole = new MasterRoot(6L, "USANONIMO", "Usuario Anónimo", null, 1L);
        List<String> permissions = permissionService.resolvePermissions(anonRole);

        assertEquals(2, permissions.size());
        assertTrue(permissions.contains("VIEW_MARKETPLACE"));
        assertTrue(permissions.contains("AUTO_ASSIGN_CONSUMER_ROLE"));
    }

    @Test
    @DisplayName("Should return empty list for unknown role code")
    void shouldReturnEmptyPermissionsForUnknownRole() {
        MasterRoot unknownRole = new MasterRoot(99L, "UNKNOWN", "Desconocido", null, 1L);
        List<String> permissions = permissionService.resolvePermissions(unknownRole);

        assertTrue(permissions.isEmpty());
    }

    // ===============================================
    // Tests for resolvePermissions(List<MasterRoot>)
    // ===============================================

    @Test
    @DisplayName("Should resolve and merge permissions for multiple roles without duplicates")
    void shouldResolvePermissionsForMultipleRoles() {
        MasterRoot adminRole = new MasterRoot(1L, "ADMIN", "Administrador", null, 1L);
        MasterRoot cashierRole = new MasterRoot(4L, "CJTURNO", "Cajero", null, 1L);

        List<String> permissions = permissionService.resolvePermissions(List.of(adminRole, cashierRole));

        assertEquals(4, permissions.size());
        assertTrue(permissions.contains("FULL_ADMIN_ACCESS"));
        assertTrue(permissions.contains("POS_SALES"));
        assertTrue(permissions.contains("QUERY_PRODUCTS"));
        assertTrue(permissions.contains("EXPIRY_CONTROL"));
    }

    @Test
    @DisplayName("Should return empty list when role list is empty")
    void shouldReturnEmptyListWhenRoleListIsEmpty() {
        List<String> permissions = permissionService.resolvePermissions(Collections.emptyList());

        assertTrue(permissions.isEmpty());
    }

    // ==========================================
    // Tests for canAssignRole
    // ==========================================

    @Test
    @DisplayName("ROLE_ADMIN or ROLE_ADMINTI can assign any role")
    void adminCanAssignAnyRole() {
        when(treeProvider.getTree()).thenReturn(masterTree);
        setupSecurityContext("adminUser", "ROLE_ADMIN");
        MasterRoot roleToAssign = new MasterRoot(10L, "CJTURNO", "Cajero", 1L, 1L);

        boolean result = permissionService.canAssignRole(roleToAssign, 5L);

        assertTrue(result);

        setupSecurityContext("adminTiUser", "ROLE_ADMINTI");
        assertTrue(permissionService.canAssignRole(roleToAssign, null));
    }

    @Test
    @DisplayName("GERGEN throws exception if not associated to an outlet")
    void gergenWithoutOutletThrowsException() {
        when(treeProvider.getTree()).thenReturn(masterTree);
        setupSecurityContext("gergenUser", "ROLE_GERGEN");
        TenantContext.setTenantOutletId(null);
        MasterRoot roleToAssign = new MasterRoot(10L, "CJTURNO", "Cajero", 1L, 1L);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> permissionService.canAssignRole(roleToAssign, 5L));

        assertEquals("El GERGEN debe estar asociado a un outlet para asignar roles.", ex.getMessage());
    }

    @Test
    @DisplayName("GERGEN throws exception if request outlet does not match current employee outlet")
    void gergenDifferentOutletThrowsException() {
        when(treeProvider.getTree()).thenReturn(masterTree);
        setupSecurityContext("gergenUser", "ROLE_GERGEN");
        TenantContext.setTenantOutletId(10L);
        MasterRoot roleToAssign = new MasterRoot(10L, "CJTURNO", "Cajero", 1L, 1L);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> permissionService.canAssignRole(roleToAssign, 20L));

        assertEquals("Un GERGEN solo puede asignar roles dentro de su propio outlet.", ex.getMessage());
    }

    @Test
    @DisplayName("GERGEN throws exception if role is not an operative role (not child of OPT)")
    void gergenNonOperativeRoleThrowsException() {
        when(treeProvider.getTree()).thenReturn(masterTree);
        setupSecurityContext("gergenUser", "ROLE_GERGEN");
        TenantContext.setTenantOutletId(10L);

        MasterRoot roleToAssign = new MasterRoot(10L, "GERGEN", "Gerente", 1L, 1L);
        when(masterTree.isChildOf(roleToAssign, "OPT")).thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> permissionService.canAssignRole(roleToAssign, 10L));

        assertEquals("Un GERGEN solo puede asignar roles operativos.", ex.getMessage());
    }

    @Test
    @DisplayName("GERGEN can assign operative role within same outlet")
    void gergenCanAssignOperativeRoleInSameOutlet() {
        when(treeProvider.getTree()).thenReturn(masterTree);
        setupSecurityContext("gergenUser", "ROLE_GERGEN");
        TenantContext.setTenantOutletId(10L);

        MasterRoot roleToAssign = new MasterRoot(10L, "CJTURNO", "Cajero", 1L, 1L);
        when(masterTree.isChildOf(roleToAssign, "OPT")).thenReturn(true);

        boolean result = permissionService.canAssignRole(roleToAssign, 10L);

        assertTrue(result);
    }

    @Test
    @DisplayName("Consumer or Anonymous can self-assign customer role when requestOutletId is null")
    void consumerCanSelfAssignCustomerRole() {
        when(treeProvider.getTree()).thenReturn(masterTree);
        setupSecurityContext("anonUser", "ROLE_USANONIMO");

        MasterRoot customerRole = new MasterRoot(20L, "CSTNDR", "Cliente Estándar", 2L, 1L);
        when(masterTree.isChildOf(customerRole, "CLIENTE")).thenReturn(true);

        boolean result = permissionService.canAssignRole(customerRole, null);

        assertTrue(result);
    }

    @Test
    @DisplayName("Other roles return false for canAssignRole")
    void otherRolesReturnFalseForCanAssignRole() {
        when(treeProvider.getTree()).thenReturn(masterTree);
        setupSecurityContext("opUser", "ROLE_CJTURNO");

        MasterRoot roleToAssign = new MasterRoot(20L, "CSTNDR", "Cliente", 2L, 1L);
        boolean result = permissionService.canAssignRole(roleToAssign, 10L);

        assertFalse(result);
    }

    // ==========================================
    // Tests for canAutoAssignConsumerRole
    // ==========================================

    @Test
    @DisplayName("canAutoAssignConsumerRole returns true when user is ANONIMO/CSTNDR and role is CSTNDR child of CLIENTE")
    void canAutoAssignConsumerRoleSuccess() {
        when(treeProvider.getTree()).thenReturn(masterTree);
        setupSecurityContext("anonUser", "ROLE_ANONIMO");

        MasterRoot cstndrRole = new MasterRoot(20L, "CSTNDR", "Cliente Estándar", 2L, 1L);
        when(masterTree.isChildOf(cstndrRole, "CLIENTE")).thenReturn(true);

        boolean result = permissionService.canAutoAssignConsumerRole(cstndrRole);

        assertTrue(result);
    }

    @Test
    @DisplayName("canAutoAssignConsumerRole returns false when user lacks ANONIMO or CSTNDR role")
    void canAutoAssignConsumerRoleFailsForOtherUsers() {
        when(treeProvider.getTree()).thenReturn(masterTree);
        setupSecurityContext("adminUser", "ROLE_ADMIN");

        MasterRoot cstndrRole = new MasterRoot(20L, "CSTNDR", "Cliente Estándar", 2L, 1L);

        boolean result = permissionService.canAutoAssignConsumerRole(cstndrRole);

        assertFalse(result);
    }

    @Test
    @DisplayName("canAutoAssignConsumerRole returns false when role shortName is not CSTNDR")
    void canAutoAssignConsumerRoleFailsForNonCstndrRole() {
        when(treeProvider.getTree()).thenReturn(masterTree);
        setupSecurityContext("anonUser", "ROLE_ANONIMO");

        MasterRoot nonCstndrRole = new MasterRoot(21L, "OTHER", "Otro Rol", 2L, 1L);

        boolean result = permissionService.canAutoAssignConsumerRole(nonCstndrRole);

        assertFalse(result);
    }
}

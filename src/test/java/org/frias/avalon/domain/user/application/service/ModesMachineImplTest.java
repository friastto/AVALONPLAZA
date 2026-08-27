package org.frias.avalon.domain.user.application.service;

import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.outlet.application.dto.response.OutletInfoDto;
import org.frias.avalon.domain.outlet.domain.model.OutletDomain;
import org.frias.avalon.domain.outlet.domain.port.OutletRepositoryPort;
import org.frias.avalon.domain.user.application.dtos.response.modes.ModesResponseDto;
import org.frias.avalon.domain.user.application.dtos.results.AdminAvalonResult;
import org.frias.avalon.domain.user.application.dtos.results.ClientResult;
import org.frias.avalon.domain.user.application.dtos.results.EmployeeResult;
import org.frias.avalon.domain.user.application.dtos.results.ModesResult;
import org.frias.avalon.domain.user.domain.model.RoleAssignmentDomain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests for ModesMachineImpl")
class ModesMachineImplTest {

    @Mock
    private MasterTreeProvider treeProvider;

    @Mock
    private PermissionService permissionService;

    @Mock
    private OutletRepositoryPort outletPort;

    @Mock
    private MasterTree masterTree;

    @InjectMocks
    private ModesMachineImpl modesMachine;

    @BeforeEach
    void setUp() {
        lenient().when(treeProvider.getTree()).thenReturn(masterTree);
    }

    @Test
    @DisplayName("Should resolve anonymous client result when role list is empty and USANONIMO role exists in tree")
    void resolveAnonymousClientWhenRoleListEmptyAndRoleExists() {
        MasterRoot anonRole = new MasterRoot(100L, "USANONIMO", "Usuario Anónimo", null, 1L);
        when(masterTree.getByCode("USANONIMO")).thenReturn(anonRole);
        when(permissionService.resolvePermissions(anonRole)).thenReturn(List.of("VIEW_MARKETPLACE", "AUTO_ASSIGN_CONSUMER_ROLE"));

        ModesResult result = modesMachine.resolve(Collections.emptyList(), null);

        assertNotNull(result);
        assertNotNull(result.client());
        assertNull(result.employee());
        assertNull(result.adminAvalon());

        assertEquals("Usuario Anónimo", result.client().type());
        assertTrue(result.client().status());
        assertEquals(2, result.client().permissions().size());
        assertTrue(result.client().permissions().contains("VIEW_MARKETPLACE"));
    }

    @Test
    @DisplayName("Should fallback to generic ClientResult when USANONIMO role is null in tree")
    void resolveAnonymousClientFallbackWhenRoleNotFound() {
        when(masterTree.getByCode("USANONIMO")).thenReturn(null);

        ModesResult result = modesMachine.resolve(Collections.emptyList(), null);

        assertNotNull(result);
        assertNotNull(result.client());
        assertNull(result.employee());
        assertNull(result.adminAvalon());

        assertEquals("Cliente Anónimo", result.client().type());
        assertTrue(result.client().status());
        assertTrue(result.client().permissions().isEmpty());
    }

    @Test
    @DisplayName("Should resolve Consumer, Employee, and AdminAvalon modes for assigned roles")
    void resolveAssignedRolesForConsumerEmployeeAndAdmin() {
        RoleAssignmentDomain consRoleAssign = new RoleAssignmentDomain(1L, 10L, 201L, 1L, 1L);
        RoleAssignmentDomain empRoleAssign = new RoleAssignmentDomain(2L, 10L, 202L, 1L, 1L);
        RoleAssignmentDomain adminRoleAssign = new RoleAssignmentDomain(3L, 10L, 203L, 1L, 1L);

        MasterRoot consRole = new MasterRoot(201L, "CSTNDR", "Cliente Estándar", 10L, 1L);
        MasterRoot empRole = new MasterRoot(202L, "CJTURNO", "Cajero de Turno", 11L, 1L);
        MasterRoot adminRole = new MasterRoot(203L, "ADMINTI", "Administrador TI", 12L, 1L);

        when(masterTree.getById(201L)).thenReturn(consRole);
        when(masterTree.getById(202L)).thenReturn(empRole);
        when(masterTree.getById(203L)).thenReturn(adminRole);

        when(masterTree.isChildOf(consRole, "CONS")).thenReturn(true);

        when(masterTree.isChildOf(empRole, "CONS")).thenReturn(false);
        when(masterTree.isChildOf(empRole, "EMP")).thenReturn(true);

        when(masterTree.isChildOf(adminRole, "CONS")).thenReturn(false);
        when(masterTree.isChildOf(adminRole, "EMP")).thenReturn(false);
        when(masterTree.isChildOf(adminRole, "SISTEM")).thenReturn(true);

        when(permissionService.resolvePermissions(consRole)).thenReturn(List.of("BUY_PRODUCTS"));
        when(permissionService.resolvePermissions(empRole)).thenReturn(List.of("POS_SALES"));
        when(permissionService.resolvePermissions(adminRole)).thenReturn(List.of("FULL_ADMIN_ACCESS"));

        OutletDomain outlet = new OutletDomain(
                5L, "OUT-1", "Tienda Central", "Av. Principal 123", "555-1234", "900123456",
                1L, null, BigDecimal.TEN, true, BigDecimal.ONE, 1L, LocalDateTime.now(), null
        );

        ModesResult result = modesMachine.resolve(List.of(consRoleAssign, empRoleAssign, adminRoleAssign), outlet);

        assertNotNull(result.client());
        assertEquals("Cliente Estándar", result.client().type());
        assertTrue(result.client().permissions().contains("BUY_PRODUCTS"));

        assertNotNull(result.employee());
        assertTrue(result.employee().status());
        assertNotNull(result.employee().outlet());
        assertEquals(5L, result.employee().outlet().id());
        assertEquals("Tienda Central", result.employee().outlet().name());
        assertEquals(empRole, result.employee().role());
        assertTrue(result.employee().permissions().contains("POS_SALES"));

        assertNotNull(result.adminAvalon());
        assertTrue(result.adminAvalon().status());
        assertNotNull(result.adminAvalon().outlet());
        assertEquals(5L, result.adminAvalon().outlet().id());
        assertEquals(adminRole, result.adminAvalon().role());
        assertTrue(result.adminAvalon().permissions().contains("FULL_ADMIN_ACCESS"));
    }

    @Test
    @DisplayName("Should handle SYSTEM code variant for admin role and null outletDomain")
    void resolveAdminAvalonWithSystemCodeVariantAndNullOutlet() {
        RoleAssignmentDomain adminRoleAssign = new RoleAssignmentDomain(1L, 10L, 301L, null, 1L);
        MasterRoot adminRole = new MasterRoot(301L, "ADMIN", "Administrador Global", 12L, 1L);

        when(masterTree.getById(301L)).thenReturn(adminRole);
        when(masterTree.isChildOf(adminRole, "CONS")).thenReturn(false);
        when(masterTree.isChildOf(adminRole, "EMP")).thenReturn(false);
        when(masterTree.isChildOf(adminRole, "SISTEM")).thenReturn(false);
        when(masterTree.isChildOf(adminRole, "SYSTEM")).thenReturn(true);
        when(permissionService.resolvePermissions(adminRole)).thenReturn(List.of("FULL_ADMIN_ACCESS"));

        ModesResult result = modesMachine.resolve(List.of(adminRoleAssign), null);

        assertNull(result.client());
        assertNull(result.employee());
        assertNotNull(result.adminAvalon());
        assertNull(result.adminAvalon().outlet());
        assertEquals(adminRole, result.adminAvalon().role());
    }

    @Test
    @DisplayName("Should skip invalid role IDs that are not present in MasterTree")
    void resolveSkipsRoleNotPresentInTree() {
        RoleAssignmentDomain invalidRoleAssign = new RoleAssignmentDomain(1L, 10L, 999L, 1L, 1L);
        when(masterTree.getById(999L)).thenReturn(null);

        ModesResult result = modesMachine.resolve(List.of(invalidRoleAssign), null);

        assertNull(result.client());
        assertNull(result.employee());
        assertNull(result.adminAvalon());
    }

    @Test
    @DisplayName("Should correctly map ModesResult to ModesResponseDto when all modes are populated")
    void mapperToResponseWithAllModesPresent() {
        ClientResult clientResult = new ClientResult("Cliente Estándar", true, List.of("BUY_PRODUCTS"));

        MasterRoot empRole = new MasterRoot(202L, "CJTURNO", "Cajero de Turno", 11L, 1L);
        OutletInfoDto outletDto = new OutletInfoDto(10L, "Sucursal Norte");
        EmployeeResult employeeResult = new EmployeeResult(true, outletDto, empRole, List.of("POS_SALES"));

        MasterRoot adminRole = new MasterRoot(203L, "ADMIN", "Administrador Global", 12L, 1L);
        AdminAvalonResult adminResult = new AdminAvalonResult(true, outletDto, adminRole, List.of("FULL_ADMIN_ACCESS"));

        ModesResult modesResult = new ModesResult(clientResult, employeeResult, adminResult);

        ModesResponseDto response = modesMachine.mapperToResponse(modesResult);

        assertNotNull(response.client());
        assertEquals("Cliente Estándar", response.client().type());
        assertTrue(response.client().enabled());
        assertEquals(List.of("BUY_PRODUCTS"), response.client().permissions());

        assertNotNull(response.employee());
        assertTrue(response.employee().enabled());
        assertEquals(10L, response.employee().store().id());
        assertEquals("CJTURNO", response.employee().role().shortName());
        assertEquals("Cajero de Turno", response.employee().role().fullName());

        assertNotNull(response.adminAvalon());
        assertTrue(response.adminAvalon().enabled());
        assertEquals(10L, response.adminAvalon().store().id());
        assertEquals("ADMIN", response.adminAvalon().role().shortName());
    }

    @Test
    @DisplayName("Should map ModesResult to ModesResponseDto with nulls when modes are empty")
    void mapperToResponseWithNullModes() {
        ModesResult emptyResult = new ModesResult(null, null, null);

        ModesResponseDto response = modesMachine.mapperToResponse(emptyResult);

        assertNull(response.client());
        assertNull(response.employee());
        assertNull(response.adminAvalon());
    }
}

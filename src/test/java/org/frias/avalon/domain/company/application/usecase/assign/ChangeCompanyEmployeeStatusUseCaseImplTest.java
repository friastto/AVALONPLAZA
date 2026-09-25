package org.frias.avalon.domain.company.application.usecase.assign;

import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.domain.company.application.dto.request.UpdateCompanyEmployeeStatusRequest;
import org.frias.avalon.domain.company.application.dto.response.CompanyEmployeeResponse;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.outlet.domain.model.OutletDomain;
import org.frias.avalon.domain.outlet.domain.port.OutletRepositoryPort;
import org.frias.avalon.domain.person.domain.model.PersonDomain;
import org.frias.avalon.domain.person.domain.port.PersonRepositoryPort;
import org.frias.avalon.domain.user.domain.model.RoleAssignmentDomain;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.frias.avalon.domain.user.domain.port.RoleAssignmentRepositoryPort;
import org.frias.avalon.domain.user.domain.port.UserAvalonRepositoryPort;
import org.frias.avalon.core.jwt.service.SessionRevocationRegistry;
import org.frias.avalon.domain.user.presentation.UserSessionWebSocketPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChangeCompanyEmployeeStatusUseCaseImplTest {

    @Mock
    private RoleAssignmentRepositoryPort roleAssignmentPort;

    @Mock
    private OutletRepositoryPort outletPort;

    @Mock
    private UserAvalonRepositoryPort userPort;

    @Mock
    private PersonRepositoryPort personPort;

    @Mock
    private MasterTreeProvider masterTreeProvider;

    @Mock
    private SessionRevocationRegistry sessionRevocationRegistry;

    @Mock
    private UserSessionWebSocketPublisher userSessionWebSocketPublisher;

    @InjectMocks
    private ChangeCompanyEmployeeStatusUseCaseImpl useCase;

    private MasterTree tree;

    @BeforeEach
    void setUp() {
        MasterRoot actStatus = new MasterRoot(1L, "ACT", "ACTIVO", null, 1L);
        MasterRoot inaStatus = new MasterRoot(4L, "INA", "INACTIVO", null, 1L);
        MasterRoot suspStatus = new MasterRoot(5L, "SUSP", "SUSPENDIDO", null, 1L);
        MasterRoot roleRoot = new MasterRoot(90L, "ADMOULT", "ADMIN_TIENDA", 86L, 1L);
        MasterRoot parentRole = new MasterRoot(86L, "GERENTE", "TYPE_GERENTES", 80L, 1L);
        MasterRoot ccType = new MasterRoot(10L, "CC", "CEDULA_CIUDADANIA", null, 1L);

        tree = new MasterTree(List.of(actStatus, inaStatus, suspStatus, roleRoot, parentRole, ccType));
        lenient().when(masterTreeProvider.getTree()).thenReturn(tree);
    }

    @Test
    void shouldDeactivateEmployeeRoleAssignmentSuccessfully() {
        Long companyId = 1L;
        Long userId = 100L;
        Long outletId = 10L;

        UpdateCompanyEmployeeStatusRequest request = new UpdateCompanyEmployeeStatusRequest("INA", outletId);

        OutletDomain outlet = OutletDomain.fromPersistence(outletId, "OULT-001", "Tienda 1", "Calle 1", "3001234567", "123456", 1L, null, null, false, BigDecimal.ZERO, companyId, null, null);
        when(outletPort.findByCompanyId(companyId)).thenReturn(List.of(outlet));

        RoleAssignmentDomain assignment = new RoleAssignmentDomain(1L, userId, 90L, outletId, null, 1L);
        when(roleAssignmentPort.findByUserAvalonId(userId)).thenReturn(List.of(assignment));

        UserAvalonDomain user = new UserAvalonDomain(userId, 50L, "cajero.juan", "salt", "hash", 1L);
        when(userPort.findById(userId)).thenReturn(Optional.of(user));

        PersonDomain person = PersonDomain.createFromEntity(50L, "12345678", "Juan", "Perez", "Calle 5", 1L, 10L, 3001112233L, "juan@test.com", 1L, null, null);
        when(personPort.findById(50L)).thenReturn(Optional.of(person));

        CompanyEmployeeResponse response = useCase.execute(companyId, userId, request);

        assertNotNull(response);
        assertEquals(userId, response.userId());
        assertEquals(4L, response.statusId());
        assertEquals("INACTIVO", response.statusName());
        assertEquals("Tienda 1", response.outletName());
        assertEquals(4L, assignment.getStatus());
        verify(roleAssignmentPort).update(assignment);
    }

    @Test
    void shouldReactivateEmployeeRoleAssignmentSuccessfully() {
        Long companyId = 1L;
        Long userId = 100L;

        UpdateCompanyEmployeeStatusRequest request = new UpdateCompanyEmployeeStatusRequest("ACT", null);

        OutletDomain outlet = OutletDomain.fromPersistence(10L, "OULT-001", "Tienda 1", "Calle 1", "3001234567", "123456", 1L, null, null, false, BigDecimal.ZERO, companyId, null, null);
        when(outletPort.findByCompanyId(companyId)).thenReturn(List.of(outlet));

        RoleAssignmentDomain assignment = new RoleAssignmentDomain(1L, userId, 90L, 10L, null, 4L);
        when(roleAssignmentPort.findByUserAvalonId(userId)).thenReturn(List.of(assignment));

        UserAvalonDomain user = new UserAvalonDomain(userId, 50L, "cajero.juan", "salt", "hash", 1L);
        when(userPort.findById(userId)).thenReturn(Optional.of(user));

        CompanyEmployeeResponse response = useCase.execute(companyId, userId, request);

        assertNotNull(response);
        assertEquals(1L, response.statusId());
        assertEquals("ACTIVO", response.statusName());
        assertEquals(1L, assignment.getStatus());
        verify(roleAssignmentPort).update(assignment);
    }

    @Test
    void shouldThrowExceptionWhenStatusNotFound() {
        Long companyId = 1L;
        Long userId = 100L;
        UpdateCompanyEmployeeStatusRequest request = new UpdateCompanyEmployeeStatusRequest("INVALID_STATUS", null);

        assertThrows(ResourceNotFoundException.class, () -> useCase.execute(companyId, userId, request));
        verify(roleAssignmentPort, never()).update(any());
    }

    @Test
    void shouldThrowExceptionWhenAssignmentNotFound() {
        Long companyId = 1L;
        Long userId = 100L;
        UpdateCompanyEmployeeStatusRequest request = new UpdateCompanyEmployeeStatusRequest("INA", null);

        when(outletPort.findByCompanyId(companyId)).thenReturn(List.of());
        when(roleAssignmentPort.findByUserAvalonId(userId)).thenReturn(List.of());

        assertThrows(ResourceNotFoundException.class, () -> useCase.execute(companyId, userId, request));
        verify(roleAssignmentPort, never()).update(any());
    }
}

package org.frias.avalon.domain.company.application.usecase.assign;

import org.frias.avalon.core.exeptions.BusinessException;
import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.domain.company.application.dto.request.TransferCompanyEmployeeRequest;
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
class TransferCompanyEmployeeUseCaseImplTest {

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
    private TransferCompanyEmployeeUseCaseImpl useCase;

    private MasterTree tree;

    @BeforeEach
    void setUp() {
        MasterRoot actStatus = new MasterRoot(1L, "ACT", "ACTIVO", null, 1L);
        MasterRoot roleRoot = new MasterRoot(90L, "ADMOULT", "ADMIN_TIENDA", 86L, 1L);
        MasterRoot parentRole = new MasterRoot(86L, "GERENTE", "TYPE_GERENTES", 80L, 1L);
        MasterRoot ccType = new MasterRoot(10L, "CC", "CEDULA_CIUDADANIA", null, 1L);

        tree = new MasterTree(List.of(actStatus, roleRoot, parentRole, ccType));
        lenient().when(masterTreeProvider.getTree()).thenReturn(tree);
    }

    @Test
    void shouldTransferEmployeeToTargetOutletSuccessfully() {
        Long companyId = 1L;
        Long userId = 100L;
        Long originOutletId = 10L;
        Long targetOutletId = 20L;

        TransferCompanyEmployeeRequest request = new TransferCompanyEmployeeRequest(targetOutletId);

        OutletDomain originOutlet = OutletDomain.fromPersistence(originOutletId, "OULT-001", "Sede Norte", "Calle 1", "3001234567", "123456", 1L, null, null, false, BigDecimal.ZERO, companyId, null, null);
        OutletDomain targetOutlet = OutletDomain.fromPersistence(targetOutletId, "OULT-002", "Sede Sur", "Calle 20", "3007654321", "654321", 1L, null, null, false, BigDecimal.ZERO, companyId, null, null);

        when(outletPort.findById(targetOutletId)).thenReturn(Optional.of(targetOutlet));
        when(outletPort.findByCompanyId(companyId)).thenReturn(List.of(originOutlet, targetOutlet));

        RoleAssignmentDomain assignment = new RoleAssignmentDomain(1L, userId, 90L, originOutletId, null, 1L);
        when(roleAssignmentPort.findByUserAvalonId(userId)).thenReturn(List.of(assignment));

        UserAvalonDomain user = new UserAvalonDomain(userId, 50L, "cajero.juan", "salt", "hash", 1L);
        when(userPort.findById(userId)).thenReturn(Optional.of(user));

        PersonDomain person = PersonDomain.createFromEntity(50L, "12345678", "Juan", "Perez", "Calle 5", 1L, 10L, 3001112233L, "juan@test.com", 1L, null, null);
        when(personPort.findById(50L)).thenReturn(Optional.of(person));

        CompanyEmployeeResponse response = useCase.execute(companyId, userId, request);

        assertNotNull(response);
        assertEquals(userId, response.userId());
        assertEquals(targetOutletId, response.outletId());
        assertEquals("Sede Sur", response.outletName());
        assertEquals(targetOutletId, assignment.getOutletId());
        verify(roleAssignmentPort).update(assignment);
    }

    @Test
    void shouldThrowExceptionWhenTargetOutletNotFound() {
        Long companyId = 1L;
        Long userId = 100L;
        Long targetOutletId = 999L;
        TransferCompanyEmployeeRequest request = new TransferCompanyEmployeeRequest(targetOutletId);

        when(outletPort.findById(targetOutletId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> useCase.execute(companyId, userId, request));
        verify(roleAssignmentPort, never()).update(any());
    }

    @Test
    void shouldThrowExceptionWhenTargetOutletBelongsToDifferentCompany() {
        Long companyId = 1L;
        Long userId = 100L;
        Long targetOutletId = 20L;
        TransferCompanyEmployeeRequest request = new TransferCompanyEmployeeRequest(targetOutletId);

        OutletDomain foreignOutlet = OutletDomain.fromPersistence(targetOutletId, "OULT-999", "Tienda Otra Empresa", "Calle 99", "3009998877", "999888", 1L, null, null, false, BigDecimal.ZERO, 2L, null, null);
        when(outletPort.findById(targetOutletId)).thenReturn(Optional.of(foreignOutlet));

        assertThrows(BusinessException.class, () -> useCase.execute(companyId, userId, request));
        verify(roleAssignmentPort, never()).update(any());
    }

    @Test
    void shouldThrowExceptionWhenEmployeeHasNoAssignmentInCompany() {
        Long companyId = 1L;
        Long userId = 100L;
        Long targetOutletId = 20L;
        TransferCompanyEmployeeRequest request = new TransferCompanyEmployeeRequest(targetOutletId);

        OutletDomain targetOutlet = OutletDomain.fromPersistence(targetOutletId, "OULT-002", "Sede Sur", "Calle 20", "3007654321", "654321", 1L, null, null, false, BigDecimal.ZERO, companyId, null, null);
        when(outletPort.findById(targetOutletId)).thenReturn(Optional.of(targetOutlet));
        when(outletPort.findByCompanyId(companyId)).thenReturn(List.of(targetOutlet));
        when(roleAssignmentPort.findByUserAvalonId(userId)).thenReturn(List.of());

        assertThrows(ResourceNotFoundException.class, () -> useCase.execute(companyId, userId, request));
        verify(roleAssignmentPort, never()).update(any());
    }
}

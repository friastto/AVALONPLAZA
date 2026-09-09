package org.frias.avalon.domain.company.application.usecase.assign;

import org.frias.avalon.domain.company.application.dto.request.AssignCompanyEmployeeRequest;
import org.frias.avalon.domain.company.application.dto.response.CompanyEmployeeResponse;
import org.frias.avalon.domain.company.domain.model.CompanyDomain;
import org.frias.avalon.domain.company.domain.port.CompanyRepositoryPort;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssignCompanyEmployeeUseCaseImplTest {

    @Mock
    private CompanyRepositoryPort companyRepositoryPort;

    @Mock
    private OutletRepositoryPort outletRepositoryPort;

    @Mock
    private PersonRepositoryPort personRepositoryPort;

    @Mock
    private UserAvalonRepositoryPort userAvalonRepositoryPort;

    @Mock
    private RoleAssignmentRepositoryPort roleAssignmentRepositoryPort;

    @Mock
    private MasterTreeProvider masterTreeProvider;

    @InjectMocks
    private AssignCompanyEmployeeUseCaseImpl useCase;

    private MasterTree tree;

    @BeforeEach
    void setUp() {
        MasterRoot rootRole = new MasterRoot(80L, "ROL", "TYPE_ROL", null, 1L);
        MasterRoot gerRole = new MasterRoot(86L, "GERENTE", "TYPE_GERENTES", 80L, 1L);
        MasterRoot gergenRole = new MasterRoot(87L, "GERGEN", "GERENTE_GENERAL", 86L, 1L);
        MasterRoot admoultRole = new MasterRoot(90L, "ADMOULT", "ADMIN_TIENDA", 86L, 1L);
        MasterRoot actStatus = new MasterRoot(1L, "ACT", "ACTIVO", null, 1L);
        MasterRoot inactStatus = new MasterRoot(4L, "INACT", "INACTIVO", null, 1L);

        tree = new MasterTree(List.of(rootRole, gerRole, gergenRole, admoultRole, actStatus, inactStatus));
        lenient().when(masterTreeProvider.getTree()).thenReturn(tree);
    }

    @Test
    void shouldAssignStoreEmployeeSuccessfully() {
        Long companyId = 1L;
        Long outletId = 10L;
        CompanyDomain company = new CompanyDomain(companyId, "Empresa Central", "900123456-1", "empresa@test.com", 1L, null, null, null);
        when(companyRepositoryPort.findById(companyId)).thenReturn(Optional.of(company));

        OutletDomain outlet = OutletDomain.fromPersistence(outletId, "OULT-001", "Tienda 1", "Calle 1", "3001234567", "123456", 1L, null, null, false, BigDecimal.ZERO, companyId, null, null);
        when(outletRepositoryPort.findById(outletId)).thenReturn(Optional.of(outlet));

        PersonDomain person = PersonDomain.createFromEntity(200L, "12345678", "Juan", "Perez", "Calle 5", 1L, 1L, 3001112233L, "juan@test.com", 1L, null, null);
        when(personRepositoryPort.findByNumberid("12345678")).thenReturn(Optional.of(person));

        UserAvalonDomain user = new UserAvalonDomain(500L, 200L, "juan.perez", "salt", "hash", 1L);
        when(userAvalonRepositoryPort.findByPersonNumberid("12345678")).thenReturn(Optional.of(user));

        AssignCompanyEmployeeRequest request = new AssignCompanyEmployeeRequest(
                1L,
                "12345678",
                "Juan",
                "Perez",
                "juan@test.com",
                3001112233L,
                "Calle 5",
                1L,
                "juan.perez",
                "pass123",
                90L, // ADMOULT
                outletId
        );

        CompanyEmployeeResponse response = useCase.execute(companyId, request);

        assertNotNull(response);
        assertEquals("Juan", response.name());
        assertEquals("ADMOULT", response.roleCode());
        assertEquals("GERENTE", response.roleCategory());
        assertEquals("Tienda 1", response.outletName());
        verify(roleAssignmentRepositoryPort).create(any(RoleAssignmentDomain.class));
    }

    @Test
    void shouldAssignCorporateGeneralManagerSuccessfully() {
        Long companyId = 1L;
        CompanyDomain company = new CompanyDomain(companyId, "Empresa Central", "900123456-1", "empresa@test.com", 1L, null, null, null);
        when(companyRepositoryPort.findById(companyId)).thenReturn(Optional.of(company));

        PersonDomain person = PersonDomain.createFromEntity(200L, "87654321", "Maria", "Gomez", "Calle 10", 1L, 1L, 3009998877L, "maria@test.com", 1L, null, null);
        when(personRepositoryPort.findByNumberid("87654321")).thenReturn(Optional.of(person));

        UserAvalonDomain user = new UserAvalonDomain(501L, 200L, "maria.gergen", "salt", "hash", 1L);
        when(userAvalonRepositoryPort.findByPersonNumberid("87654321")).thenReturn(Optional.of(user));

        AssignCompanyEmployeeRequest request = new AssignCompanyEmployeeRequest(
                1L,
                "87654321",
                "Maria",
                "Gomez",
                "maria@test.com",
                3009998877L,
                "Calle 10",
                1L,
                "maria.gergen",
                "pass123",
                87L, // GERGEN
                null
        );

        CompanyEmployeeResponse response = useCase.execute(companyId, request);

        assertNotNull(response);
        assertEquals("Maria", response.name());
        assertEquals("GERGEN", response.roleCode());
        assertEquals("Sede Corporativa / Empresa", response.outletName());
        verify(roleAssignmentRepositoryPort).create(any(RoleAssignmentDomain.class));
    }
}

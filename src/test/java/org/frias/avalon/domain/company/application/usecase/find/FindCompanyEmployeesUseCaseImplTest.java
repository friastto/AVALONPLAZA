package org.frias.avalon.domain.company.application.usecase.find;

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
class FindCompanyEmployeesUseCaseImplTest {

    @Mock
    private RoleAssignmentRepositoryPort roleAssignmentRepositoryPort;

    @Mock
    private OutletRepositoryPort outletRepositoryPort;

    @Mock
    private UserAvalonRepositoryPort userAvalonRepositoryPort;

    @Mock
    private PersonRepositoryPort personRepositoryPort;

    @Mock
    private MasterTreeProvider masterTreeProvider;

    @InjectMocks
    private FindCompanyEmployeesUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        MasterRoot rootRole = new MasterRoot(80L, "ROL", "TYPE_ROL", null, 1L);
        MasterRoot gerRole = new MasterRoot(86L, "GERENTE", "TYPE_GERENTES", 80L, 1L);
        MasterRoot gergenRole = new MasterRoot(87L, "GERGEN", "GERENTE_GENERAL", 86L, 1L);
        MasterRoot actStatus = new MasterRoot(1L, "ACT", "ACTIVO", null, 1L);

        MasterTree tree = new MasterTree(List.of(rootRole, gerRole, gergenRole, actStatus));
        lenient().when(masterTreeProvider.getTree()).thenReturn(tree);
    }

    @Test
    void shouldFindAllEmployeesForCompany() {
        Long companyId = 1L;
        OutletDomain outlet = OutletDomain.fromPersistence(10L, "OULT-001", "Tienda Centro", "Calle 1", "3001234567", "123456", 1L, null, null, false, BigDecimal.ZERO, companyId, null, null);
        when(outletRepositoryPort.findByCompanyId(companyId)).thenReturn(List.of(outlet));

        RoleAssignmentDomain gergenAssignment = new RoleAssignmentDomain(1L, 100L, 87L, null, companyId, 1L);
        when(roleAssignmentRepositoryPort.findByCompanyId(companyId)).thenReturn(List.of(gergenAssignment));
        when(roleAssignmentRepositoryPort.findByOutletId(10L)).thenReturn(List.of());

        UserAvalonDomain user = UserAvalonDomain.createWithPerson("gerente.general", "salt", "hash", 1L, 200L);
        when(userAvalonRepositoryPort.findById(100L)).thenReturn(Optional.of(user));

        PersonDomain person = PersonDomain.createFromEntity(200L, "11223344", "Carlos", "Mendoza", "Calle 10", 1L, 1L, 3105551234L, "carlos@empresa.com", 1L, null, null);
        when(personRepositoryPort.findById(200L)).thenReturn(Optional.of(person));

        List<CompanyEmployeeResponse> employees = useCase.execute(companyId);

        assertNotNull(employees);
        assertEquals(1, employees.size());
        CompanyEmployeeResponse emp = employees.get(0);
        assertEquals("Carlos", emp.name());
        assertEquals("GERGEN", emp.roleCode());
        assertEquals("GERENTE", emp.roleCategory());
        assertEquals("Sede Corporativa / Empresa", emp.outletName());
    }
}

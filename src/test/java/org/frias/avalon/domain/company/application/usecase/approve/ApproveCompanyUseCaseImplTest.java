package org.frias.avalon.domain.company.application.usecase.approve;

import org.frias.avalon.core.tenant.port.TenantSchemaMigrationPort;
import org.frias.avalon.domain.company.application.dto.response.CompanyResponse;
import org.frias.avalon.domain.company.domain.model.CompanyDomain;
import org.frias.avalon.domain.company.domain.port.CompanyRepositoryPort;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.outlet.domain.model.LocationDomain;
import org.frias.avalon.domain.outlet.domain.model.OutletDomain;
import org.frias.avalon.domain.outlet.domain.port.OutletRepositoryPort;
import org.frias.avalon.domain.user.domain.model.RoleAssignmentDomain;
import org.frias.avalon.domain.user.domain.port.RoleAssignmentRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias para ApproveCompanyUseCaseImpl")
class ApproveCompanyUseCaseImplTest {

    @Mock
    private CompanyRepositoryPort companyPort;

    @Mock
    private TenantSchemaMigrationPort tenantSchemaMigrationPort;

    @Mock
    private MasterTreeProvider masterTreeProvider;

    @Mock
    private OutletRepositoryPort outletPort;

    @Mock
    private RoleAssignmentRepositoryPort roleAssignmentRepository;

    @InjectMocks
    private ApproveCompanyUseCaseImpl approveCompanyUseCase;

    @Test
    @DisplayName("Deberia aprobar una empresa exitosamente, migrar tenant y activar tiendas y gerente")
    void shouldApproveCompanySuccessfullyWhenCompanyExists() {
        // Arrange
        Long companyId = 10L;
        LocalDateTime now = LocalDateTime.now();

        CompanyDomain existingCompany = new CompanyDomain(
                companyId,
                "900123456-1",
                "Empresa Ejemplo S.A.S.",
                "contacto@ejemplo.com",
                2L, // Pendiente de aprobacion
                new BigDecimal("1000000.00"),
                now,
                now
        );

        CompanyDomain approvedCompany = new CompanyDomain(
                companyId,
                "900123456-1",
                "Empresa Ejemplo S.A.S.",
                "contacto@ejemplo.com",
                1L, // Aprobado (1L)
                new BigDecimal("1000000.00"),
                now,
                now
        );

        given(companyPort.findById(companyId)).willReturn(Optional.of(existingCompany));
        given(companyPort.save(any(CompanyDomain.class))).willReturn(approvedCompany);

        MasterRoot actNode = new MasterRoot(1L, "ACT", "ACTIVO", null, 1L);
        MasterRoot gergenNode = new MasterRoot(50L, "GERGEN", "GERENTE GENERAL", null, 1L);
        MasterTree masterTree = new MasterTree(List.of(actNode, gergenNode));
        given(masterTreeProvider.getTree()).willReturn(masterTree);

        OutletDomain inactiveOutlet = OutletDomain.create(
                "Tienda Inicial",
                "Calle 123",
                "3001234567",
                "900123456-1",
                2L,
                new LocationDomain(4.6, -74.0),
                BigDecimal.ZERO,
                companyId
        );
        given(outletPort.findByCompanyId(companyId)).willReturn(List.of(inactiveOutlet));

        RoleAssignmentDomain inactiveRole = RoleAssignmentDomain.createCompanyRole(100L, 50L, companyId, 2L);
        given(roleAssignmentRepository.findByCompanyIdAndRoleId(companyId, 50L)).willReturn(Optional.of(inactiveRole));

        // Act
        CompanyResponse response = approveCompanyUseCase.execute(companyId);

        // Assert
        assertNotNull(response);
        assertEquals(companyId, response.id());
        assertEquals("900123456-1", response.nit());
        assertEquals("Empresa Ejemplo S.A.S.", response.name());
        assertEquals("contacto@ejemplo.com", response.email());
        assertEquals(1L, response.statusId());
        assertEquals(new BigDecimal("1000000.00"), response.defaultCashThresholdAmount());

        ArgumentCaptor<CompanyDomain> captor = ArgumentCaptor.forClass(CompanyDomain.class);
        verify(companyPort).save(captor.capture());
        assertEquals(1L, captor.getValue().statusId());

        verify(companyPort).findById(companyId);
        verify(tenantSchemaMigrationPort).migrateTenantSchema("company_10");
        verify(outletPort).update(any(OutletDomain.class));
        verify(roleAssignmentRepository).update(any(RoleAssignmentDomain.class));
    }

    @Test
    @DisplayName("Deberia lanzar IllegalArgumentException cuando la empresa no existe")
    void shouldThrowIllegalArgumentExceptionWhenCompanyNotFound() {
        // Arrange
        Long companyId = 99L;
        given(companyPort.findById(companyId)).willReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> approveCompanyUseCase.execute(companyId)
        );

        assertEquals("Company with ID 99 not found", exception.getMessage());
        verify(companyPort).findById(companyId);
        verifyNoMoreInteractions(companyPort);
        verifyNoInteractions(tenantSchemaMigrationPort);
    }
}


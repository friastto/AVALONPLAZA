package org.frias.avalon.domain.company.application.usecase.approve;

import org.frias.avalon.core.tenant.port.TenantSchemaMigrationPort;
import org.frias.avalon.domain.company.application.dto.response.CompanyResponse;
import org.frias.avalon.domain.company.domain.model.CompanyDomain;
import org.frias.avalon.domain.company.domain.port.CompanyRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    @InjectMocks
    private ApproveCompanyUseCaseImpl approveCompanyUseCase;

    @Test
    @DisplayName("Deberia aprobar una empresa exitosamente y migrar el esquema tenant")
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

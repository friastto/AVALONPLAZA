package org.frias.avalon.domain.company.application.usecase.create;

import org.frias.avalon.domain.company.application.dto.request.CreateCompanyServiceRequestDto;
import org.frias.avalon.domain.company.application.dto.response.CompanyResponse;
import org.frias.avalon.domain.company.domain.model.CompanyDomain;
import org.frias.avalon.domain.company.domain.port.CompanyRepositoryPort;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.outlet.domain.model.OutletDomain;
import org.frias.avalon.domain.outlet.domain.port.OutletRepositoryPort;
import org.frias.avalon.domain.user.domain.model.RoleAssignmentDomain;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.frias.avalon.domain.user.domain.port.RoleAssignmentRepositoryPort;
import org.frias.avalon.domain.user.domain.port.UserAvalonRepositoryPort;
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
@DisplayName("Pruebas Unitarias para CreateCompanyServiceRequestUseCaseImpl")
class CreateCompanyServiceRequestUseCaseImplTest {

    @Mock
    private CompanyRepositoryPort companyPort;

    @Mock
    private OutletRepositoryPort outletPort;

    @Mock
    private UserAvalonRepositoryPort userRepository;

    @Mock
    private RoleAssignmentRepositoryPort roleAssignmentRepository;

    @Mock
    private MasterTreeProvider masterTreeProvider;

    @InjectMocks
    private CreateCompanyServiceRequestUseCaseImpl useCase;

    @Test
    @DisplayName("Deberia crear la solicitud de servicio con tienda inicial y postulacion de gerente")
    void shouldCreateCompanyServiceRequestSuccessfully() {
        // Arrange
        CreateCompanyServiceRequestDto request = new CreateCompanyServiceRequestDto(
                "Mi Super Empresa S.A.S.",
                "900999888-1",
                "contacto@empresa.com",
                "Sede Principal Norte",
                "Carrera 15 # 85-30",
                "3101234567",
                4.6789,
                -74.0567,
                200L,
                "token-abc-123"
        );

        given(companyPort.findByNit("900999888-1")).willReturn(Optional.empty());

        UserAvalonDomain applicant = new UserAvalonDomain(
                200L,
                300L,
                "solicitante1",
                "salt",
                "hashed",
                1L
        );
        given(userRepository.findById(200L)).willReturn(Optional.of(applicant));


        MasterRoot rvwNode = new MasterRoot(2L, "RVW", "EN REVISION", null, 1L);
        MasterRoot inaNode = new MasterRoot(3L, "INA", "INACTIVO", null, 1L);
        MasterRoot actNode = new MasterRoot(1L, "ACT", "ACTIVO", null, 1L);
        MasterRoot gergenRole = new MasterRoot(50L, "GERGEN", "GERENTE GENERAL", null, 1L);
        MasterTree tree = new MasterTree(List.of(rvwNode, inaNode, actNode, gergenRole));
        given(masterTreeProvider.getTree()).willReturn(tree);

        CompanyDomain savedCompany = new CompanyDomain(
                10L,
                "900999888-1",
                "Mi Super Empresa S.A.S.",
                "contacto@empresa.com",
                2L,
                BigDecimal.ZERO,
                LocalDateTime.now(),
                null
        );
        given(companyPort.save(any(CompanyDomain.class))).willReturn(savedCompany);

        // Act
        CompanyResponse response = useCase.execute(request);

        // Assert
        assertNotNull(response);
        assertEquals(10L, response.id());
        assertEquals("900999888-1", response.nit());
        assertEquals("Mi Super Empresa S.A.S.", response.name());
        assertEquals(2L, response.statusId());

        // Verificar que la tienda inicial se guardo en estado inactivo con coordenadas
        ArgumentCaptor<OutletDomain> outletCaptor = ArgumentCaptor.forClass(OutletDomain.class);
        verify(outletPort).save(outletCaptor.capture());
        OutletDomain savedOutlet = outletCaptor.getValue();
        assertEquals("Sede Principal Norte", savedOutlet.getName());
        assertEquals("Carrera 15 # 85-30", savedOutlet.getAddress());
        assertEquals("900999888-1", savedOutlet.getNit());
        assertEquals(3L, savedOutlet.getStatusId());
        assertEquals(10L, savedOutlet.getCompanyId());
        assertEquals(4.6789, savedOutlet.getLocation().latitude());
        assertEquals(-74.0567, savedOutlet.getLocation().longitude());

        // Verificar que se postulo el rol GERGEN inactivo para el solicitante
        ArgumentCaptor<RoleAssignmentDomain> roleCaptor = ArgumentCaptor.forClass(RoleAssignmentDomain.class);
        verify(roleAssignmentRepository).create(roleCaptor.capture());
        RoleAssignmentDomain savedRole = roleCaptor.getValue();
        assertEquals(200L, savedRole.getUserId());
        assertEquals(50L, savedRole.getRoleId());
        assertEquals(10L, savedRole.getCompanyId());
        assertEquals(3L, savedRole.getStatus());
    }

    @Test
    @DisplayName("Deberia fallar si el NIT ya existe")
    void shouldThrowExceptionWhenNitAlreadyExists() {
        CreateCompanyServiceRequestDto request = new CreateCompanyServiceRequestDto(
                "Mi Super Empresa S.A.S.",
                "900999888-1",
                "contacto@empresa.com",
                "Sede Principal",
                "Calle 1",
                "3101234567",
                4.6,
                -74.0,
                200L,
                null
        );

        CompanyDomain existing = new CompanyDomain(1L, "900999888-1", "Otra", null, 1L, BigDecimal.ZERO, null, null);
        given(companyPort.findByNit("900999888-1")).willReturn(Optional.of(existing));

        assertThrows(IllegalStateException.class, () -> useCase.execute(request));
        verify(companyPort, never()).save(any());
        verify(outletPort, never()).save(any());
        verify(roleAssignmentRepository, never()).create(any());
    }
}

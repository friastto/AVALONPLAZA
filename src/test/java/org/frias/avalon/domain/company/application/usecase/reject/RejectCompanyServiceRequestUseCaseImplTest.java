package org.frias.avalon.domain.company.application.usecase.reject;

import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.core.notification.EmailServicePort;
import org.frias.avalon.domain.company.application.dto.request.RejectCompanyServiceRequestDto;
import org.frias.avalon.domain.company.domain.model.CompanyDomain;
import org.frias.avalon.domain.company.domain.port.CompanyRepositoryPort;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.outlet.domain.model.LocationDomain;
import org.frias.avalon.domain.outlet.domain.model.OutletDomain;
import org.frias.avalon.domain.outlet.domain.port.OutletRepositoryPort;
import org.frias.avalon.domain.person.domain.model.PersonDomain;
import org.frias.avalon.domain.person.domain.port.PersonRepositoryPort;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias para RejectCompanyServiceRequestUseCaseImpl")
class RejectCompanyServiceRequestUseCaseImplTest {

    @Mock
    private CompanyRepositoryPort companyPort;

    @Mock
    private OutletRepositoryPort outletPort;

    @Mock
    private RoleAssignmentRepositoryPort roleAssignmentPort;

    @Mock
    private UserAvalonRepositoryPort userPort;

    @Mock
    private PersonRepositoryPort personPort;

    @Mock
    private MasterTreeProvider masterTreeProvider;

    @Mock
    private EmailServicePort emailServicePort;

    @InjectMocks
    private RejectCompanyServiceRequestUseCaseImpl useCase;

    @Test
    @DisplayName("Deberia rechazar la solicitud, inactivar tienda y enviar correo con motivo y explicacion")
    void shouldRejectCompanyRequestAndSendEmailSuccessfully() {
        // Arrange
        Long companyId = 15L;
        RejectCompanyServiceRequestDto request = new RejectCompanyServiceRequestDto(
                "NIT_INVALIDO",
                "El NIT ingresado no coincide con el certificado de existencia y representacion legal."
        );

        CompanyDomain company = new CompanyDomain(
                companyId, "900111222-3", "Empresa Ejemplo", "info@ejemplo.com",
                50L, BigDecimal.ZERO, LocalDateTime.now(), LocalDateTime.now()
        );
        given(companyPort.findById(companyId)).willReturn(Optional.of(company));

        MasterTree tree = mock(MasterTree.class);
        MasterRoot recNode = mock(MasterRoot.class);
        MasterRoot inaNode = mock(MasterRoot.class);
        MasterRoot gergenRoleNode = mock(MasterRoot.class);

        given(masterTreeProvider.getTree()).willReturn(tree);
        given(tree.getByCode("REC")).willReturn(recNode);
        given(recNode.getId()).willReturn(60L);
        given(tree.getByCodeOrThrow("INA")).willReturn(inaNode);
        given(inaNode.getId()).willReturn(70L);
        given(tree.getByCodeOrThrow("GERGEN")).willReturn(gergenRoleNode);
        given(gergenRoleNode.getId()).willReturn(80L);

        OutletDomain outlet = OutletDomain.create(
                "Tienda Ejemplo", "Calle 1", "3001112233", "900111222-3",
                50L, new LocationDomain(4.6, -74.0), BigDecimal.ZERO, companyId
        );
        given(outletPort.findByCompanyId(companyId)).willReturn(List.of(outlet));

        RoleAssignmentDomain assignment = mock(RoleAssignmentDomain.class);
        given(assignment.getUserId()).willReturn(100L);
        given(roleAssignmentPort.findByCompanyIdAndRoleId(companyId, 80L)).willReturn(Optional.of(assignment));

        UserAvalonDomain user = mock(UserAvalonDomain.class);
        given(user.getPersonId()).willReturn(200L);
        given(userPort.findById(100L)).willReturn(Optional.of(user));

        PersonDomain person = mock(PersonDomain.class);
        given(person.getName()).willReturn("Pedro");
        given(person.getLastName()).willReturn("Perez");
        given(person.getEmail()).willReturn("pedro.perez@ejemplo.com");
        given(personPort.findById(200L)).willReturn(Optional.of(person));

        // Act
        useCase.execute(companyId, request);

        // Assert
        ArgumentCaptor<CompanyDomain> companyCaptor = ArgumentCaptor.forClass(CompanyDomain.class);
        verify(companyPort).save(companyCaptor.capture());
        assertEquals(60L, companyCaptor.getValue().statusId());

        verify(outletPort).update(any(OutletDomain.class));
        verify(assignment).changeStatus(70L);
        verify(roleAssignmentPort).update(assignment);

        verify(emailServicePort).sendCompanyRejectionEmail(
                eq("pedro.perez@ejemplo.com"),
                eq("Pedro Perez"),
                eq("Empresa Ejemplo"),
                eq("NIT_INVALIDO"),
                eq("El NIT ingresado no coincide con el certificado de existencia y representacion legal.")
        );
    }

    @Test
    @DisplayName("Deberia lanzar ResourceNotFoundException cuando la empresa a rechazar no existe")
    void shouldThrowExceptionWhenCompanyNotFound() {
        // Arrange
        Long nonExistentId = 999L;
        RejectCompanyServiceRequestDto request = new RejectCompanyServiceRequestDto("MOTIVO", "Explicacion");
        given(companyPort.findById(nonExistentId)).willReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> useCase.execute(nonExistentId, request));
    }
}

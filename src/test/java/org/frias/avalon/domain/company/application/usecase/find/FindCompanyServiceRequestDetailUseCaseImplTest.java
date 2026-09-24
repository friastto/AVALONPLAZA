package org.frias.avalon.domain.company.application.usecase.find;

import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.domain.company.application.dto.response.CompanyServiceRequestDetailResponse;
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
@DisplayName("Pruebas Unitarias para FindCompanyServiceRequestDetailUseCaseImpl")
class FindCompanyServiceRequestDetailUseCaseImplTest {

    @Mock
    private CompanyRepositoryPort companyRepositoryPort;

    @Mock
    private OutletRepositoryPort outletRepositoryPort;

    @Mock
    private RoleAssignmentRepositoryPort roleAssignmentRepositoryPort;

    @Mock
    private UserAvalonRepositoryPort userRepositoryPort;

    @Mock
    private PersonRepositoryPort personRepositoryPort;

    @Mock
    private MasterTreeProvider masterTreeProvider;

    @InjectMocks
    private FindCompanyServiceRequestDetailUseCaseImpl useCase;

    @Test
    @DisplayName("Deberia retornar el detalle completo de la solicitud con tienda GPS y celular del solicitante")
    void shouldReturnFullDetailSuccessfully() {
        // Arrange
        Long companyId = 50L;
        Long statusId = 100L;
        Long gergenRoleId = 80L;
        LocalDateTime now = LocalDateTime.now();

        CompanyDomain company = new CompanyDomain(
                companyId, "900999888-1", "Servicios Luci S.A.S.", "contacto@luci.com",
                statusId, BigDecimal.ZERO, now, now
        );

        MasterTree tree = mock(MasterTree.class);
        MasterRoot statusNode = mock(MasterRoot.class);
        MasterRoot gergenRoleNode = mock(MasterRoot.class);

        given(masterTreeProvider.getTree()).willReturn(tree);
        given(tree.getById(statusId)).willReturn(statusNode);
        given(statusNode.getId()).willReturn(statusId);
        given(statusNode.getShortName()).willReturn("RVW");
        given(statusNode.getFullName()).willReturn("EN_REVISION");

        given(tree.getByCodeOrThrow("GERGEN")).willReturn(gergenRoleNode);
        given(gergenRoleNode.getId()).willReturn(gergenRoleId);

        given(companyRepositoryPort.findById(companyId)).willReturn(Optional.of(company));

        OutletDomain initialOutlet = OutletDomain.create(
                "Luci Sede Norte", "Carrera 15 # 85-30", "3109876543", "900999888-1",
                101L, new LocationDomain(4.6789, -74.0567), BigDecimal.ZERO, companyId
        );
        given(outletRepositoryPort.findByCompanyId(companyId)).willReturn(List.of(initialOutlet));

        RoleAssignmentDomain assignment = mock(RoleAssignmentDomain.class);
        given(assignment.getUserId()).willReturn(200L);
        given(roleAssignmentRepositoryPort.findByCompanyIdAndRoleId(companyId, gergenRoleId))
                .willReturn(Optional.of(assignment));

        UserAvalonDomain user = mock(UserAvalonDomain.class);
        given(user.getPersonId()).willReturn(300L);
        given(userRepositoryPort.findById(200L)).willReturn(Optional.of(user));

        PersonDomain person = mock(PersonDomain.class);
        given(person.getName()).willReturn("Lucia");
        given(person.getLastName()).willReturn("Gomez");
        given(person.getNumberid()).willReturn("1020304050");
        given(person.getEmail()).willReturn("lucia.gomez@gmail.com");
        given(person.getPhoneNumber()).willReturn(3123456789L);
        given(personRepositoryPort.findById(300L)).willReturn(Optional.of(person));

        // Act
        CompanyServiceRequestDetailResponse response = useCase.execute(companyId);

        // Assert
        assertNotNull(response);
        assertEquals(companyId, response.companyId());
        assertEquals("Servicios Luci S.A.S.", response.companyName());
        assertEquals("900999888-1", response.companyNit());
        assertEquals("contacto@luci.com", response.companyEmail());
        assertEquals("RVW", response.status().code());

        // Outlet details
        assertEquals("Luci Sede Norte", response.storeName());
        assertEquals("Carrera 15 # 85-30", response.storeAddress());
        assertEquals("3109876543", response.storePhone());
        assertEquals(4.6789, response.latitude());
        assertEquals(-74.0567, response.longitude());

        // Applicant details
        assertEquals(200L, response.applicantUserId());
        assertEquals("Lucia Gomez", response.applicantFullName());
        assertEquals("1020304050", response.applicantIdentification());
        assertEquals("lucia.gomez@gmail.com", response.applicantEmail());
        assertEquals("3123456789", response.applicantPhone());
    }

    @Test
    @DisplayName("Deberia lanzar ResourceNotFoundException cuando la empresa no existe")
    void shouldThrowExceptionWhenCompanyNotFound() {
        // Arrange
        Long nonExistentId = 999L;
        given(companyRepositoryPort.findById(nonExistentId)).willReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> useCase.execute(nonExistentId));
    }
}

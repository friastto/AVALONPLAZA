package org.frias.avalon.domain.company.application.usecase.assign;

import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.domain.company.application.dto.request.AssignCompanyManagerRequest;
import org.frias.avalon.domain.company.application.dto.response.CompanyManagerResponse;
import org.frias.avalon.domain.company.domain.model.CompanyDomain;
import org.frias.avalon.domain.company.domain.port.CompanyRepositoryPort;
import org.frias.avalon.domain.person.domain.model.PersonDomain;
import org.frias.avalon.domain.person.domain.port.PersonRepositoryPort;
import org.frias.avalon.domain.user.domain.model.RoleAssignmentDomain;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.frias.avalon.domain.user.domain.port.RoleAssignmentRepositoryPort;
import org.frias.avalon.domain.user.domain.port.UserAvalonRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssignCompanyManagerUseCaseImplTest {

    @Mock
    private CompanyRepositoryPort companyRepository;

    @Mock
    private PersonRepositoryPort personRepository;

    @Mock
    private UserAvalonRepositoryPort userRepository;

    @Mock
    private RoleAssignmentRepositoryPort roleAssignmentRepository;

    @InjectMocks
    private AssignCompanyManagerUseCaseImpl useCase;

    private CompanyDomain testCompany;
    private PersonDomain testPerson;
    private UserAvalonDomain testUser;
    private AssignCompanyManagerRequest testRequest;

    @BeforeEach
    void setUp() {
        testCompany = new CompanyDomain(1L, "900123456", "Empresa Test SAS", "test@empresa.com", 1L, BigDecimal.valueOf(500000), LocalDateTime.now(), LocalDateTime.now());
        testPerson = PersonDomain.createFromEntity(10L, "123456789", "Carlos", "Gomez", "Calle 1", 1L, 1L, 3001234567L, "carlos@test.com", 1L, LocalDateTime.now(), LocalDateTime.now());
        testUser = UserAvalonDomain.createWithPerson("carlos123", "salt", "hashed", 1L, 10L);

        testRequest = new AssignCompanyManagerRequest(
                null,
                null,
                1L,
                "123456789",
                "Carlos",
                "Gomez",
                "Calle 1",
                1L,
                "carlos@test.com",
                "3001234567",
                "carlos123",
                "Secret123*"
        );
    }

    @Test
    @DisplayName("Debe asignar exitosamente un nuevo gerente creando persona y usuario")
    void testAssignCompanyManagerSuccess() {
        when(companyRepository.findById(1L)).thenReturn(Optional.of(testCompany));
        when(personRepository.findByNumberid("123456789")).thenReturn(Optional.empty());
        when(personRepository.save(any(PersonDomain.class))).thenReturn(testPerson);

        when(userRepository.findByPersonNumberid("123456789")).thenReturn(Optional.empty());
        when(userRepository.save(any(UserAvalonDomain.class))).thenAnswer(invocation -> {
            UserAvalonDomain u = invocation.getArgument(0);
            return new UserAvalonDomain(20L, u.getPersonId(), u.getUserName(), u.getHashSalt(), u.getHashPassword(), u.getStatusId());
        });

        when(roleAssignmentRepository.findByCompanyIdAndRoleId(1L, 87L)).thenReturn(Optional.empty());

        RoleAssignmentDomain savedAssignment = new RoleAssignmentDomain(100L, 20L, 87L, null, 1L, 1L);
        when(roleAssignmentRepository.create(any(RoleAssignmentDomain.class))).thenReturn(savedAssignment);

        CompanyManagerResponse response = useCase.execute(1L, testRequest);

        assertNotNull(response);
        assertEquals(1L, response.companyId());
        assertEquals(20L, response.userId());
        assertEquals(10L, response.personId());
        assertEquals("GERGEN", response.roleCode());
        assertEquals(87L, response.roleId());
        verify(roleAssignmentRepository, times(1)).create(any(RoleAssignmentDomain.class));
    }

    @Test
    @DisplayName("Debe fallar si la compania no existe")
    void testCompanyNotFoundThrowsException() {
        when(companyRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> useCase.execute(99L, testRequest));
        verify(roleAssignmentRepository, never()).create(any());
    }
}

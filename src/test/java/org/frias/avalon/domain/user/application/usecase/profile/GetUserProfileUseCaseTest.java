package org.frias.avalon.domain.user.application.usecase.profile;

import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.person.domain.model.PersonDomain;
import org.frias.avalon.domain.person.domain.port.PersonRepositoryPort;
import org.frias.avalon.domain.user.application.dtos.response.UserProfileResponseDto;
import org.frias.avalon.domain.user.domain.model.RoleAssignmentDomain;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.frias.avalon.domain.user.domain.port.RoleAssignmentRepositoryPort;
import org.frias.avalon.domain.user.domain.port.UserAvalonRepositoryPort;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests for GetUserProfileUseCaseImpl")
class GetUserProfileUseCaseTest {

    @Mock
    private UserAvalonRepositoryPort userPort;

    @Mock
    private PersonRepositoryPort personPort;

    @Mock
    private RoleAssignmentRepositoryPort roleAssignmentPort;

    @Mock
    private MasterTreeProvider masterTreeProvider;

    @Mock
    private MasterTree masterTree;

    @InjectMocks
    private GetUserProfileUseCaseImpl getUserProfileUseCase;

    private UserAvalonDomain sampleUser;
    private PersonDomain samplePerson;
    private MasterRoot userStatusNode;
    private MasterRoot idTypeNode;
    private MasterRoot roleNode;

    @BeforeEach
    void setUp() {
        sampleUser = UserAvalonDomain.fromPersistenceBasic(10L, 20L, "carlos.rod", 1L);
        samplePerson = PersonDomain.createFromEntity(
                20L,
                "1098765432",
                "Carlos",
                "Rodriguez",
                "Calle 45 # 12-34",
                96L,
                87L,
                3001234567L,
                "carlos@ejemplo.com",
                1L,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        userStatusNode = new MasterRoot(1L, "ACT", "ACTIVO", null, 1L);
        idTypeNode = new MasterRoot(96L, "CC", "CEDULA DE CIUDADANIA", null, 1L);
        roleNode = new MasterRoot(150L, "CLIENTE", "CONSUMIDOR STANDARD", null, 1L);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("execute with valid username returns full profile with person and roles enriched")
    void execute_withValidUsername_returnsFullProfile() {
        when(userPort.findByUserName("carlos.rod")).thenReturn(Optional.of(sampleUser));
        when(personPort.findById(20L)).thenReturn(Optional.of(samplePerson));
        when(masterTreeProvider.getTree()).thenReturn(masterTree);
        when(masterTree.getById(1L)).thenReturn(userStatusNode);
        when(masterTree.getById(96L)).thenReturn(idTypeNode);

        RoleAssignmentDomain assignment = new RoleAssignmentDomain(1L, 10L, 150L, null, null, 1L);
        when(roleAssignmentPort.findByUserAvalonId(10L)).thenReturn(List.of(assignment));
        when(masterTree.is(userStatusNode, "ACT")).thenReturn(true);
        when(masterTree.getById(150L)).thenReturn(roleNode);

        UserProfileResponseDto result = getUserProfileUseCase.execute("carlos.rod");

        assertNotNull(result);
        assertEquals(10L, result.userId());
        assertEquals("carlos.rod", result.userName());
        assertEquals(20L, result.personId());
        assertEquals("Carlos", result.name());
        assertEquals("Rodriguez", result.lastName());
        assertEquals("Carlos Rodriguez", result.fullName());
        assertEquals("carlos@ejemplo.com", result.email());
        assertEquals(3001234567L, result.phoneNumber());
        assertEquals("Calle 45 # 12-34", result.address());
        assertEquals("1098765432", result.numberId());

        assertNotNull(result.identificationType());
        assertEquals("CC", result.identificationType().code());
        assertEquals("CEDULA DE CIUDADANIA", result.identificationType().name());

        assertNotNull(result.status());
        assertEquals("ACT", result.status().code());

        assertEquals(1, result.roles().size());
        assertEquals("CLIENTE", result.roles().get(0).code());
        assertNotNull(result.activeRole());
        assertEquals("CLIENTE", result.activeRole().code());
    }

    @Test
    @DisplayName("execute with numeric identifier finds user by id")
    void execute_withNumericIdentifier_findsById() {
        when(userPort.findById(10L)).thenReturn(Optional.of(sampleUser));
        when(personPort.findById(20L)).thenReturn(Optional.of(samplePerson));
        when(masterTreeProvider.getTree()).thenReturn(masterTree);
        when(masterTree.getById(1L)).thenReturn(userStatusNode);
        when(masterTree.getById(96L)).thenReturn(idTypeNode);
        when(roleAssignmentPort.findByUserAvalonId(10L)).thenReturn(Collections.emptyList());

        UserProfileResponseDto result = getUserProfileUseCase.execute("10");

        assertNotNull(result);
        assertEquals(10L, result.userId());
        assertEquals("carlos.rod", result.userName());
        assertTrue(result.roles().isEmpty());
        assertNull(result.activeRole());
    }

    @Test
    @DisplayName("execute with user having no person record handles nulls gracefully")
    void execute_withUserHavingNoPerson_handlesNullsGracefully() {
        UserAvalonDomain userWithoutPerson = UserAvalonDomain.fromPersistenceBasic(11L, null, "admin.sys", 1L);
        when(userPort.findByUserName("admin.sys")).thenReturn(Optional.of(userWithoutPerson));
        when(masterTreeProvider.getTree()).thenReturn(masterTree);
        when(masterTree.getById(1L)).thenReturn(userStatusNode);
        when(roleAssignmentPort.findByUserAvalonId(11L)).thenReturn(Collections.emptyList());

        UserProfileResponseDto result = getUserProfileUseCase.execute("admin.sys");

        assertNotNull(result);
        assertEquals(11L, result.userId());
        assertNull(result.personId());
        assertNull(result.name());
        assertNull(result.lastName());
        assertEquals("admin.sys", result.fullName());
        assertNull(result.email());
        assertNull(result.identificationType());
    }

    @Test
    @DisplayName("execute with blank identifier throws IllegalArgumentException")
    void execute_withBlankIdentifier_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> getUserProfileUseCase.execute(""));
        assertThrows(IllegalArgumentException.class, () -> getUserProfileUseCase.execute("   "));
        assertThrows(IllegalArgumentException.class, () -> getUserProfileUseCase.execute(null));
    }

    @Test
    @DisplayName("execute with nonexistent user throws ResourceNotFoundException")
    void execute_withNonexistentUser_throwsResourceNotFoundException() {
        when(userPort.findByUserName("desconocido")).thenReturn(Optional.empty());
        when(userPort.findByIdentifier("desconocido")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> getUserProfileUseCase.execute("desconocido"));
    }

    @Test
    @DisplayName("executeCurrent with authenticated security context returns profile")
    void executeCurrent_withAuthenticatedContext_returnsProfile() {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken("carlos.rod", "password", Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(userPort.findByUserName("carlos.rod")).thenReturn(Optional.of(sampleUser));
        when(personPort.findById(20L)).thenReturn(Optional.of(samplePerson));
        when(masterTreeProvider.getTree()).thenReturn(masterTree);
        when(masterTree.getById(1L)).thenReturn(userStatusNode);
        when(masterTree.getById(96L)).thenReturn(idTypeNode);
        when(roleAssignmentPort.findByUserAvalonId(10L)).thenReturn(Collections.emptyList());

        UserProfileResponseDto result = getUserProfileUseCase.executeCurrent();

        assertNotNull(result);
        assertEquals("carlos.rod", result.userName());
    }

    @Test
    @DisplayName("executeCurrent without authentication throws ResourceNotFoundException")
    void executeCurrent_withoutAuthentication_throwsResourceNotFoundException() {
        SecurityContextHolder.clearContext();

        assertThrows(ResourceNotFoundException.class, () -> getUserProfileUseCase.executeCurrent());
    }
}

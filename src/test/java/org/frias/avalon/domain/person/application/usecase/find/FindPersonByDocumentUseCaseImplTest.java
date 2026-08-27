package org.frias.avalon.domain.person.application.usecase.find;

import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.outlet.domain.model.OutletDomain;
import org.frias.avalon.domain.outlet.domain.port.OutletRepositoryPort;
import org.frias.avalon.domain.person.application.dto.response.PersonDetailResponseDto;
import org.frias.avalon.domain.person.domain.model.PersonDomain;
import org.frias.avalon.domain.person.domain.port.PersonRepositoryPort;
import org.frias.avalon.domain.user.domain.model.RoleAssignmentDomain;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.frias.avalon.domain.user.domain.port.RoleAssignmentRepositoryPort;
import org.frias.avalon.domain.user.domain.port.UserAvalonRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias completas para FindPersonByDocumentUseCaseImpl.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias para FindPersonByDocumentUseCaseImpl")
class FindPersonByDocumentUseCaseImplTest {

    @Mock
    private PersonRepositoryPort personRepositoryPort;

    @Mock
    private UserAvalonRepositoryPort userAvalonRepositoryPort;

    @Mock
    private RoleAssignmentRepositoryPort roleAssignmentRepositoryPort;

    @Mock
    private OutletRepositoryPort outletRepositoryPort;

    @Mock
    private MasterTreeProvider masterTreeProvider;

    @Mock
    private MasterTree masterTree;

    @InjectMocks
    private FindPersonByDocumentUseCaseImpl findPersonByDocumentUseCase;

    private static final String DOCUMENT_NUMBER = "1098765432";
    private PersonDomain samplePerson;

    @BeforeEach
    void setUp() {
        samplePerson = PersonDomain.createFromEntity(
                1L,
                DOCUMENT_NUMBER,
                "JUAN",
                "PEREZ",
                "CALLE 123",
                10L, // typeIdentificationId
                20L, // sexId
                3001234567L,
                "juan.perez@example.com",
                1L,
                null,
                null
        );
    }

    @Nested
    @DisplayName("Escenarios de Búsqueda de Persona")
    class PersonSearchScenarios {

        @Test
        @DisplayName("Debe retornar DTO con personExists=false cuando la persona no existe")
        void shouldReturnPersonNotExistsDtoWhenPersonNotFound() {
            // Arrange
            when(personRepositoryPort.findByNumberid(DOCUMENT_NUMBER)).thenReturn(Optional.empty());

            // Act
            PersonDetailResponseDto result = findPersonByDocumentUseCase.execute(DOCUMENT_NUMBER);

            // Assert
            assertNotNull(result);
            assertFalse(result.personExists());
            assertFalse(result.userExists());
            assertNull(result.personId());
            assertNull(result.name());
            assertNull(result.email());

            verify(personRepositoryPort, times(1)).findByNumberid(DOCUMENT_NUMBER);
            verifyNoInteractions(masterTreeProvider, userAvalonRepositoryPort, roleAssignmentRepositoryPort, outletRepositoryPort);
        }

        @Test
        @DisplayName("Debe retornar DTO de persona sin usuario cuando la persona existe pero no tiene usuario vinculado")
        void shouldReturnPersonOnlyDtoWhenUserNotFound() {
            // Arrange
            MasterRoot typeIdRoot = MasterRoot.fromPersistence(10L, "CC", "CEDULA DE CIUDADANIA", 1L, 1L);
            MasterRoot sexRoot = MasterRoot.fromPersistence(20L, "M", "MASCULINO", 2L, 1L);

            when(personRepositoryPort.findByNumberid(DOCUMENT_NUMBER)).thenReturn(Optional.of(samplePerson));
            when(masterTreeProvider.getTree()).thenReturn(masterTree);
            when(masterTree.getById(10L)).thenReturn(typeIdRoot);
            when(masterTree.getById(20L)).thenReturn(sexRoot);
            when(userAvalonRepositoryPort.findByPersonNumberid(DOCUMENT_NUMBER)).thenReturn(Optional.empty());

            // Act
            PersonDetailResponseDto result = findPersonByDocumentUseCase.execute(DOCUMENT_NUMBER);

            // Assert
            assertNotNull(result);
            assertTrue(result.personExists());
            assertFalse(result.userExists());
            assertEquals(1L, result.personId());
            assertEquals("JUAN", result.name());
            assertEquals("PEREZ", result.lastName());
            assertEquals("CALLE 123", result.address());
            assertEquals("juan.perez@example.com", result.email());
            assertEquals(3001234567L, result.phoneNumber());
            assertEquals(10L, result.typeIdentificationId());
            assertEquals("CEDULA DE CIUDADANIA", result.typeIdentificationName());
            assertEquals(20L, result.sexId());
            assertEquals("MASCULINO", result.sexName());
            assertNull(result.userId());
            assertNull(result.userName());
            assertFalse(result.hasActiveRole());

            verify(roleAssignmentRepositoryPort, never()).findByUserAvalonId(anyLong());
        }

        @Test
        @DisplayName("Debe manejar adecuadamente cuando los nodos de MasterTree retornan null")
        void shouldHandleNullMasterTreeNodesGracefully() {
            // Arrange
            when(personRepositoryPort.findByNumberid(DOCUMENT_NUMBER)).thenReturn(Optional.of(samplePerson));
            when(masterTreeProvider.getTree()).thenReturn(masterTree);
            when(masterTree.getById(10L)).thenReturn(null);
            when(masterTree.getById(20L)).thenReturn(null);
            when(userAvalonRepositoryPort.findByPersonNumberid(DOCUMENT_NUMBER)).thenReturn(Optional.empty());

            // Act
            PersonDetailResponseDto result = findPersonByDocumentUseCase.execute(DOCUMENT_NUMBER);

            // Assert
            assertNotNull(result);
            assertTrue(result.personExists());
            assertNull(result.typeIdentificationName());
            assertNull(result.sexName());
        }
    }

    @Nested
    @DisplayName("Escenarios con Usuario y Asignación de Roles")
    class UserAndRoleScenarios {

        @Test
        @DisplayName("Debe retornar DTO con datos de usuario y hasActiveRole=false cuando el usuario no tiene roles asignados")
        void shouldReturnUserDtoWithoutRoleWhenRoleAssignmentsIsEmpty() {
            // Arrange
            UserAvalonDomain user = UserAvalonDomain.fromPersistenceBasic(100L, 1L, "jperez", 1L);

            MasterRoot typeIdRoot = MasterRoot.fromPersistence(10L, "CC", "CEDULA DE CIUDADANIA", 1L, 1L);
            MasterRoot sexRoot = MasterRoot.fromPersistence(20L, "M", "MASCULINO", 2L, 1L);

            when(personRepositoryPort.findByNumberid(DOCUMENT_NUMBER)).thenReturn(Optional.of(samplePerson));
            when(masterTreeProvider.getTree()).thenReturn(masterTree);
            when(masterTree.getById(10L)).thenReturn(typeIdRoot);
            when(masterTree.getById(20L)).thenReturn(sexRoot);
            when(userAvalonRepositoryPort.findByPersonNumberid(DOCUMENT_NUMBER)).thenReturn(Optional.of(user));
            when(roleAssignmentRepositoryPort.findByUserAvalonId(100L)).thenReturn(Collections.emptyList());

            // Act
            PersonDetailResponseDto result = findPersonByDocumentUseCase.execute(DOCUMENT_NUMBER);

            // Assert
            assertNotNull(result);
            assertTrue(result.personExists());
            assertTrue(result.userExists());
            assertEquals(100L, result.userId());
            assertEquals("jperez", result.userName());
            assertFalse(result.hasActiveRole());
            assertNull(result.currentRoleName());
            assertNull(result.currentOutletName());

            verify(outletRepositoryPort, never()).findById(anyLong());
        }

        @Test
        @DisplayName("Debe retornar DTO completo con rol y tienda más reciente cuando existen asignaciones con tienda")
        void shouldReturnFullDtoWithLatestRoleAndOutlet() {
            // Arrange
            UserAvalonDomain user = UserAvalonDomain.fromPersistenceBasic(100L, 1L, "jperez", 1L);

            RoleAssignmentDomain oldAssignment = new RoleAssignmentDomain(50L, 100L, 30L, 5L, 1L);
            RoleAssignmentDomain latestAssignment = new RoleAssignmentDomain(51L, 100L, 31L, 6L, 1L);

            MasterRoot typeIdRoot = MasterRoot.fromPersistence(10L, "CC", "CEDULA DE CIUDADANIA", 1L, 1L);
            MasterRoot sexRoot = MasterRoot.fromPersistence(20L, "M", "MASCULINO", 2L, 1L);
            MasterRoot roleRoot = MasterRoot.fromPersistence(31L, "ROLE_CASHIER", "CAJERO PRINCIPAL", 3L, 1L);
            OutletDomain outlet = OutletDomain.fromPersistence(6L, "OUT-01", "Tienda Norte", "Av 1", "555-111", "900123-1", 1L, null);

            when(personRepositoryPort.findByNumberid(DOCUMENT_NUMBER)).thenReturn(Optional.of(samplePerson));
            when(masterTreeProvider.getTree()).thenReturn(masterTree);
            when(masterTree.getById(10L)).thenReturn(typeIdRoot);
            when(masterTree.getById(20L)).thenReturn(sexRoot);
            when(masterTree.getById(31L)).thenReturn(roleRoot);
            when(userAvalonRepositoryPort.findByPersonNumberid(DOCUMENT_NUMBER)).thenReturn(Optional.of(user));
            when(roleAssignmentRepositoryPort.findByUserAvalonId(100L)).thenReturn(List.of(oldAssignment, latestAssignment));
            when(outletRepositoryPort.findById(6L)).thenReturn(Optional.of(outlet));

            // Act
            PersonDetailResponseDto result = findPersonByDocumentUseCase.execute(DOCUMENT_NUMBER);

            // Assert
            assertNotNull(result);
            assertTrue(result.personExists());
            assertTrue(result.userExists());
            assertTrue(result.hasActiveRole());
            assertEquals(100L, result.userId());
            assertEquals("jperez", result.userName());
            assertEquals("CAJERO PRINCIPAL", result.currentRoleName());
            assertEquals(31L, result.currentRoleId());
            assertEquals(6L, result.currentOutletId());
            assertEquals("Tienda Norte", result.currentOutletName());
            assertEquals(51L, result.assignmentId());
        }

        @Test
        @DisplayName("Debe retornar DTO con rol y outletName nulo si la asignación no especifica outletId")
        void shouldReturnRoleWithoutOutletWhenOutletIdIsNull() {
            // Arrange
            UserAvalonDomain user = UserAvalonDomain.fromPersistenceBasic(100L, 1L, "admin_user", 1L);

            RoleAssignmentDomain assignmentWithoutOutlet = new RoleAssignmentDomain(52L, 100L, 40L, null, 1L);
            MasterRoot typeIdRoot = MasterRoot.fromPersistence(10L, "CC", "CEDULA DE CIUDADANIA", 1L, 1L);
            MasterRoot sexRoot = MasterRoot.fromPersistence(20L, "M", "MASCULINO", 2L, 1L);
            MasterRoot roleRoot = MasterRoot.fromPersistence(40L, "ROLE_ADMIN", "ADMINISTRADOR GENERAL", 3L, 1L);

            when(personRepositoryPort.findByNumberid(DOCUMENT_NUMBER)).thenReturn(Optional.of(samplePerson));
            when(masterTreeProvider.getTree()).thenReturn(masterTree);
            when(masterTree.getById(10L)).thenReturn(typeIdRoot);
            when(masterTree.getById(20L)).thenReturn(sexRoot);
            when(masterTree.getById(40L)).thenReturn(roleRoot);
            when(userAvalonRepositoryPort.findByPersonNumberid(DOCUMENT_NUMBER)).thenReturn(Optional.of(user));
            when(roleAssignmentRepositoryPort.findByUserAvalonId(100L)).thenReturn(List.of(assignmentWithoutOutlet));

            // Act
            PersonDetailResponseDto result = findPersonByDocumentUseCase.execute(DOCUMENT_NUMBER);

            // Assert
            assertNotNull(result);
            assertTrue(result.personExists());
            assertTrue(result.userExists());
            assertTrue(result.hasActiveRole());
            assertEquals("ADMINISTRADOR GENERAL", result.currentRoleName());
            assertNull(result.currentOutletId());
            assertNull(result.currentOutletName());

            verify(outletRepositoryPort, never()).findById(anyLong());
        }
    }
}

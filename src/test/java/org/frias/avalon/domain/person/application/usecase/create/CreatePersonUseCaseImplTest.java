package org.frias.avalon.domain.person.application.usecase.create;

import org.frias.avalon.core.exeptions.BusinessException;
import org.frias.avalon.domain.masterdata.application.dto.response.MasterDataResponseDto;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.person.application.dto.request.CreatePersonRequest;
import org.frias.avalon.domain.person.application.dto.response.PersonResponse;
import org.frias.avalon.domain.person.domain.model.PersonDomain;
import org.frias.avalon.domain.person.domain.port.PersonRepositoryPort;
import org.frias.avalon.domain.person.infraestructure.mapper.PersonMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias completas para CreatePersonUseCaseImpl.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias para CreatePersonUseCaseImpl")
class CreatePersonUseCaseImplTest {

    @Mock
    private PersonRepositoryPort personRepositoryPort;

    @Mock
    private PersonMapper personMapper;

    @Mock
    private MasterTreeProvider masterTreeProvider;

    @Mock
    private MasterTree masterTree;

    @InjectMocks
    private CreatePersonUseCaseImpl createPersonUseCase;

    private static final Long TYPE_ID = 10L;
    private static final Long SEX_ID = 20L;
    private static final Long STATUS_ID = 1L;

    @BeforeEach
    void setUp() {
        // En cada test donde se consulte el masterTreeProvider, retornamos la mock masterTree por defecto
        lenient().when(masterTreeProvider.getTree()).thenReturn(masterTree);
    }

    @Nested
    @DisplayName("Creación exitosa de personas")
    class SuccessfulCreation {

        @Test
        @DisplayName("Debe crear una persona correctamente con todos los campos válidos")
        void shouldCreatePersonSuccessfullyWithAllFields() {
            // Arrange
            CreatePersonRequest request = new CreatePersonRequest(
                    TYPE_ID,
                    "1098765432",
                    "Carlos",
                    "Gomez",
                    "Carrera 45 # 10-20",
                    SEX_ID,
                    3109876543L,
                    "carlos.gomez@example.com",
                    STATUS_ID
            );

            PersonDomain savedDomain = PersonDomain.createFromEntity(
                    100L,
                    "1098765432",
                    "CARLOS",
                    "GOMEZ",
                    "Carrera 45 # 10-20",
                    TYPE_ID,
                    SEX_ID,
                    3109876543L,
                    "carlos.gomez@example.com",
                    STATUS_ID,
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );

            MasterDataResponseDto dummyMasterDto = new MasterDataResponseDto(1L, "TEST", "Test Label");
            PersonResponse expectedResponse = new PersonResponse(
                    100L,
                    "1098765432",
                    "CARLOS",
                    "GOMEZ",
                    "Carrera 45 # 10-20",
                    dummyMasterDto,
                    dummyMasterDto,
                    3109876543L,
                    "carlos.gomez@example.com",
                    dummyMasterDto,
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );

            when(personRepositoryPort.save(any(PersonDomain.class))).thenReturn(savedDomain);
            when(personMapper.toResponse(savedDomain)).thenReturn(expectedResponse);

            // Act
            PersonResponse response = createPersonUseCase.execute(request);

            // Assert
            assertNotNull(response);
            assertEquals(100L, response.id());
            assertEquals("CARLOS", response.name());
            assertEquals("GOMEZ", response.lastName());

            // Capturar la persona de dominio pasada al repositorio para verificar sanitización
            ArgumentCaptor<PersonDomain> captor = ArgumentCaptor.forClass(PersonDomain.class);
            verify(personRepositoryPort, times(1)).save(captor.capture());
            PersonDomain passedPerson = captor.getValue();

            assertEquals("1098765432", passedPerson.getNumberid());
            assertEquals("CARLOS", passedPerson.getName());
            assertEquals("GOMEZ", passedPerson.getLastName());
            assertEquals("carlos.gomez@example.com", passedPerson.getEmail());

            verify(masterTree, times(1)).getByIdOrThrow(STATUS_ID);
            verify(masterTree, times(1)).getByIdOrThrow(TYPE_ID);
            verify(masterTree, times(1)).getByIdOrThrow(SEX_ID);
        }

        @Test
        @DisplayName("Debe crear una persona cuando sexId es nulo")
        void shouldCreatePersonSuccessfullyWhenSexIdIsNull() {
            // Arrange
            CreatePersonRequest request = new CreatePersonRequest(
                    TYPE_ID,
                    "987654321",
                    "Ana",
                    "Lopez",
                    "Calle 50 # 12-34",
                    null, // sexId nulo
                    3201234567L,
                    "ana.lopez@example.com",
                    STATUS_ID
            );

            PersonDomain savedDomain = PersonDomain.createFromEntity(
                    101L, "987654321", "ANA", "LOPEZ", "Calle 50 # 12-34",
                    TYPE_ID, null, 3201234567L, "ana.lopez@example.com",
                    STATUS_ID, LocalDateTime.now(), LocalDateTime.now()
            );

            PersonResponse expectedResponse = new PersonResponse(
                    101L, "987654321", "ANA", "LOPEZ", "Calle 50 # 12-34",
                    null, null, 3201234567L, "ana.lopez@example.com",
                    null, LocalDateTime.now(), LocalDateTime.now()
            );

            when(personRepositoryPort.save(any(PersonDomain.class))).thenReturn(savedDomain);
            when(personMapper.toResponse(savedDomain)).thenReturn(expectedResponse);

            // Act
            PersonResponse response = createPersonUseCase.execute(request);

            // Assert
            assertNotNull(response);
            assertEquals(101L, response.id());

            verify(masterTree, times(1)).getByIdOrThrow(STATUS_ID);
            verify(masterTree, times(1)).getByIdOrThrow(TYPE_ID);
            verify(masterTree, never()).getByIdOrThrow(SEX_ID);
        }
    }

    @Nested
    @DisplayName("Validaciones de errores y excepciones")
    class ValidationFailures {

        @Test
        @DisplayName("Debe fallar si MasterTree lanza excepción al no encontrar statusId")
        void shouldThrowExceptionWhenStatusIdNotFoundInMasterTree() {
            // Arrange
            CreatePersonRequest request = new CreatePersonRequest(
                    TYPE_ID, "12345", "Juan", "Perez", "Calle 1",
                    SEX_ID, 3000000000L, "juan@test.com", 999L
            );

            doThrow(new IllegalStateException("MasterData no encontrado: 999"))
                    .when(masterTree).getByIdOrThrow(999L);

            // Act & Assert
            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> createPersonUseCase.execute(request));

            assertTrue(ex.getMessage().contains("999"));
            verify(personRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("Debe fallar si faltan tanto el teléfono como el email (Regla de Dominio)")
        void shouldThrowBusinessExceptionWhenBothPhoneAndEmailAreMissing() {
            // Arrange
            CreatePersonRequest request = new CreatePersonRequest(
                    TYPE_ID, "12345", "Juan", "Perez", "Calle 1",
                    SEX_ID, null, null, STATUS_ID
            );

            // Act & Assert
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> createPersonUseCase.execute(request));

            assertTrue(ex.getMessage().contains("telefono o un email valido"));
            verify(personRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("Debe fallar si el email provisto no tiene formato válido (Regla de Dominio)")
        void shouldThrowBusinessExceptionWhenEmailIsInvalid() {
            // Arrange
            CreatePersonRequest request = new CreatePersonRequest(
                    TYPE_ID, "12345", "Juan", "Perez", "Calle 1",
                    SEX_ID, 3000000000L, "invalid-email-format", STATUS_ID
            );

            // Act & Assert
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> createPersonUseCase.execute(request));

            assertTrue(ex.getMessage().contains("formato del email no es valido"));
            verify(personRepositoryPort, never()).save(any());
        }
    }
}

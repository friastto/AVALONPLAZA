package org.frias.avalon.domain.person.application.usecase.changestatus;

import jakarta.persistence.EntityNotFoundException;
import org.frias.avalon.core.exeptions.BusinessException;
import org.frias.avalon.domain.masterdata.application.dto.response.MasterDataResponseDto;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.repository.MasterDataRepositoryPort;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.person.application.dto.response.PersonResponse;
import org.frias.avalon.domain.person.domain.model.PersonDomain;
import org.frias.avalon.domain.person.domain.port.PersonRepositoryPort;
import org.frias.avalon.domain.person.infraestructure.mapper.PersonMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests for ChangePersonStatusUseCaseImpl")
class ChangePersonStatusUseCaseImplTest {

    @Mock
    private PersonRepositoryPort personPort;

    @Mock
    private MasterTreeProvider treeProvider;

    @Mock
    private MasterDataRepositoryPort masterPort;

    @Mock
    private PersonMapper mapper;

    @Mock
    private MasterTree masterTree;

    private ChangePersonStatusUseCaseImpl useCase;

    private final Long personId = 10L;
    private final Long activeStatusId = 1L;
    private final Long inactiveStatusId = 2L;

    @BeforeEach
    void setUp() {
        useCase = new ChangePersonStatusUseCaseImpl(personPort, treeProvider, masterPort, mapper);
    }

    private PersonDomain createSamplePerson(Long statusId) {
        return PersonDomain.createFromEntity(
                personId, "12345678", "JUAN", "PEREZ", "CALLE 123",
                1L, 1L, 5550000L, "juan@email.com", statusId,
                LocalDateTime.now(), LocalDateTime.now()
        );
    }

    @Nested
    @DisplayName("Successful Status Change Tests")
    class SuccessTests {

        @Test
        @DisplayName("Should change status successfully when person and status transition are valid")
        void shouldChangeStatusSuccessfully() {
            PersonDomain person = createSamplePerson(activeStatusId);
            when(personPort.findById(personId)).thenReturn(Optional.of(person));

            MasterRoot oldStatus = new MasterRoot(activeStatusId, "ACT", "Activo", 0L, 1L);
            MasterRoot newStatus = new MasterRoot(inactiveStatusId, "INA", "Inactivo", 0L, 1L);

            when(masterPort.findById(activeStatusId)).thenReturn(Optional.of(oldStatus));
            when(masterPort.findById(inactiveStatusId)).thenReturn(Optional.of(newStatus));

            when(treeProvider.getTree()).thenReturn(masterTree);
            when(masterTree.isChildOf(newStatus, "STSGEN")).thenReturn(true);

            when(personPort.save(any(PersonDomain.class))).thenAnswer(inv -> inv.getArgument(0));

            MasterDataResponseDto typeIdDto = new MasterDataResponseDto(1L, "CC", "Cedula");
            MasterDataResponseDto statusDto = new MasterDataResponseDto(inactiveStatusId, "INA", "Inactivo");
            PersonResponse expectedResponse = new PersonResponse(
                    personId, "12345678", "JUAN", "PEREZ", "CALLE 123",
                    typeIdDto, null, 5550000L, "juan@email.com", statusDto,
                    LocalDateTime.now(), LocalDateTime.now()
            );
            when(mapper.toResponse(any(PersonDomain.class))).thenReturn(expectedResponse);

            PersonResponse response = useCase.execute(personId, inactiveStatusId);

            assertNotNull(response);
            assertEquals(personId, response.id());
            assertEquals("Inactivo", response.status().fullName());
            verify(personPort, times(1)).save(any(PersonDomain.class));
        }
    }

    @Nested
    @DisplayName("Validation and Exception Handling Tests")
    class ExceptionTests {

        @Test
        @DisplayName("Should throw EntityNotFoundException when person is not found")
        void shouldThrowEntityNotFoundExceptionWhenPersonMissing() {
            when(personPort.findById(personId)).thenReturn(Optional.empty());

            EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                    () -> useCase.execute(personId, inactiveStatusId));
            assertEquals("la persona no se encontro en la base de datos", ex.getMessage());
        }

        @Test
        @DisplayName("Should throw BusinessException when old status is missing in master data")
        void shouldThrowBusinessExceptionWhenOldStatusMissing() {
            PersonDomain person = createSamplePerson(activeStatusId);
            when(personPort.findById(personId)).thenReturn(Optional.of(person));
            when(masterPort.findById(activeStatusId)).thenReturn(Optional.empty());

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> useCase.execute(personId, inactiveStatusId));
            assertEquals("no se puede establecer este estado al la persona", ex.getMessage());
        }

        @Test
        @DisplayName("Should throw BusinessException when new status is missing in master data")
        void shouldThrowBusinessExceptionWhenNewStatusMissing() {
            PersonDomain person = createSamplePerson(activeStatusId);
            when(personPort.findById(personId)).thenReturn(Optional.of(person));

            MasterRoot oldStatus = new MasterRoot(activeStatusId, "ACT", "Activo", 0L, 1L);
            when(masterPort.findById(activeStatusId)).thenReturn(Optional.of(oldStatus));
            when(masterPort.findById(inactiveStatusId)).thenReturn(Optional.empty());

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> useCase.execute(personId, inactiveStatusId));
            assertEquals("no se puede establecer este estado al la persona", ex.getMessage());
        }

        @Test
        @DisplayName("Should throw IllegalStateException when new status is not child of STSGEN")
        void shouldThrowIllegalStateExceptionWhenNewStatusIsNotChildOfStsgen() {
            PersonDomain person = createSamplePerson(activeStatusId);
            when(personPort.findById(personId)).thenReturn(Optional.of(person));

            MasterRoot oldStatus = new MasterRoot(activeStatusId, "ACT", "Activo", 0L, 1L);
            MasterRoot newStatus = new MasterRoot(inactiveStatusId, "INVALID", "Invalido", 0L, 1L);

            when(masterPort.findById(activeStatusId)).thenReturn(Optional.of(oldStatus));
            when(masterPort.findById(inactiveStatusId)).thenReturn(Optional.of(newStatus));

            when(treeProvider.getTree()).thenReturn(masterTree);
            when(masterTree.isChildOf(newStatus, "STSGEN")).thenReturn(false);

            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> useCase.execute(personId, inactiveStatusId));
            assertEquals("no se puede establecer este estado", ex.getMessage());
        }
    }
}

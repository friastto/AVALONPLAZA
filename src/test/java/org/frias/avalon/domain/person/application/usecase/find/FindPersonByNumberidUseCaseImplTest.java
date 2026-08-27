package org.frias.avalon.domain.person.application.usecase.find;

import jakarta.persistence.EntityNotFoundException;
import org.frias.avalon.domain.masterdata.application.dto.response.MasterDataResponseDto;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests for FindPersonByNumberidUseCaseImpl")
class FindPersonByNumberidUseCaseImplTest {

    @Mock
    private PersonRepositoryPort personRepositoryPort;

    @Mock
    private PersonMapper personMapper;

    private FindPersonByNumberidUseCaseImpl useCase;

    private final String numberid = "12345678";

    @BeforeEach
    void setUp() {
        useCase = new FindPersonByNumberidUseCaseImpl(personRepositoryPort, personMapper);
    }

    private PersonDomain createSamplePerson() {
        return PersonDomain.createFromEntity(
                1L, numberid, "MARIA", "LOPEZ", "AV MAIN 45",
                1L, 2L, 5558888L, "maria@email.com", 1L,
                LocalDateTime.now(), LocalDateTime.now()
        );
    }

    @Nested
    @DisplayName("Successful Retrieval Tests")
    class SuccessTests {

        @Test
        @DisplayName("Should find person by numberid and return PersonResponse")
        void shouldFindPersonByNumberidSuccessfully() {
            PersonDomain person = createSamplePerson();
            when(personRepositoryPort.findByNumberid(numberid)).thenReturn(Optional.of(person));

            MasterDataResponseDto typeIdDto = new MasterDataResponseDto(1L, "CC", "Cedula");
            MasterDataResponseDto sexDto = new MasterDataResponseDto(2L, "FEM", "Femenino");
            MasterDataResponseDto statusDto = new MasterDataResponseDto(1L, "ACT", "Activo");

            PersonResponse expectedResponse = new PersonResponse(
                    1L, numberid, "MARIA", "LOPEZ", "AV MAIN 45",
                    typeIdDto, sexDto, 5558888L, "maria@email.com", statusDto,
                    LocalDateTime.now(), LocalDateTime.now()
            );
            when(personMapper.toResponse(person)).thenReturn(expectedResponse);

            PersonResponse response = useCase.execute(numberid);

            assertNotNull(response);
            assertEquals(1L, response.id());
            assertEquals(numberid, response.numberid());
            assertEquals("MARIA", response.name());
            assertEquals("LOPEZ", response.lastName());
        }
    }

    @Nested
    @DisplayName("Exception Handling Tests")
    class ExceptionTests {

        @Test
        @DisplayName("Should throw EntityNotFoundException when person is not found")
        void shouldThrowEntityNotFoundExceptionWhenPersonNotFound() {
            when(personRepositoryPort.findByNumberid(numberid)).thenReturn(Optional.empty());

            EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                    () -> useCase.execute(numberid));
            assertTrue(ex.getMessage().contains("Persona no encontrada con número de identificación"));
        }
    }
}

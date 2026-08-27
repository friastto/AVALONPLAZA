package org.frias.avalon.domain.person.infraestructure.persistence.adapter;

import org.frias.avalon.domain.person.domain.model.PersonDomain;
import org.frias.avalon.domain.person.infraestructure.mapper.PersonMapper;
import org.frias.avalon.domain.person.infraestructure.persistence.entity.PersonEntity;
import org.frias.avalon.domain.person.infraestructure.persistence.repository.JpaPersonRepository;
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
@DisplayName("Unit Tests for PersonRepositoryAdapter")
class PersonRepositoryAdapterTest {

    @Mock
    private JpaPersonRepository jpaPersonRepository;

    @Mock
    private PersonMapper personMapper;

    private PersonRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new PersonRepositoryAdapter(jpaPersonRepository, personMapper);
    }

    private PersonDomain createSampleDomain() {
        return PersonDomain.createFromEntity(
                1L, "12345678", "CARLOS", "RAMIREZ", "AV 5",
                1L, 1L, 5553333L, "carlos@email.com", 1L,
                LocalDateTime.now(), LocalDateTime.now()
        );
    }

    private PersonEntity createSampleEntity() {
        return PersonEntity.builder()
                .id(1L)
                .numberId("12345678")
                .name("CARLOS")
                .lastName("RAMIREZ")
                .address("AV 5")
                .identificationId(1L)
                .sexId(1L)
                .phoneNumber(5553333L)
                .email("carlos@email.com")
                .statusId(1L)
                .build();
    }

    @Nested
    @DisplayName("Repository Adapter CRUD Operations")
    class CrudOperations {

        @Test
        @DisplayName("Should map and save person entity correctly")
        void shouldSavePersonSuccessfully() {
            PersonDomain domain = createSampleDomain();
            PersonEntity entity = createSampleEntity();

            when(personMapper.toEntity(domain)).thenReturn(entity);
            when(jpaPersonRepository.save(entity)).thenReturn(entity);
            when(personMapper.toDomain(entity)).thenReturn(domain);

            PersonDomain saved = adapter.save(domain);

            assertNotNull(saved);
            assertEquals(1L, saved.getId());
            assertEquals("CARLOS", saved.getName());
            verify(jpaPersonRepository, times(1)).save(entity);
        }

        @Test
        @DisplayName("Should find person by id correctly")
        void shouldFindByIdSuccessfully() {
            PersonEntity entity = createSampleEntity();
            PersonDomain domain = createSampleDomain();

            when(jpaPersonRepository.findById(1L)).thenReturn(Optional.of(entity));
            when(personMapper.toDomain(entity)).thenReturn(domain);

            Optional<PersonDomain> result = adapter.findById(1L);

            assertTrue(result.isPresent());
            assertEquals(1L, result.get().getId());
        }

        @Test
        @DisplayName("Should find person by numberid correctly")
        void shouldFindByNumberidSuccessfully() {
            PersonEntity entity = createSampleEntity();
            PersonDomain domain = createSampleDomain();

            when(jpaPersonRepository.findByNumberId("12345678")).thenReturn(Optional.of(entity));
            when(personMapper.toDomain(entity)).thenReturn(domain);

            Optional<PersonDomain> result = adapter.findByNumberid("12345678");

            assertTrue(result.isPresent());
            assertEquals("12345678", result.get().getNumberid());
        }
    }
}

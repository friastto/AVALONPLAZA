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
@DisplayName("Unit Tests for PersonPersistenceAdapter")
class PersonPersistenceAdapterTest {

    @Mock
    private JpaPersonRepository jpaPersonRepository;

    @Mock
    private PersonMapper personMapper;

    private PersonPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new PersonPersistenceAdapter(jpaPersonRepository, personMapper);
    }

    private PersonDomain createSampleDomain() {
        return PersonDomain.createFromEntity(
                2L, "87654321", "ANA", "TORRES", "STREET 9",
                1L, 2L, 5554444L, "ana@email.com", 1L,
                LocalDateTime.now(), LocalDateTime.now()
        );
    }

    private PersonEntity createSampleEntity() {
        return PersonEntity.builder()
                .id(2L)
                .numberId("87654321")
                .name("ANA")
                .lastName("TORRES")
                .address("STREET 9")
                .identificationId(1L)
                .sexId(2L)
                .phoneNumber(5554444L)
                .email("ana@email.com")
                .statusId(1L)
                .build();
    }

    @Nested
    @DisplayName("Persistence Adapter Operations")
    class Operations {

        @Test
        @DisplayName("Should save person entity correctly in PersonPersistenceAdapter")
        void shouldSavePersonSuccessfully() {
            PersonDomain domain = createSampleDomain();
            PersonEntity entity = createSampleEntity();

            when(personMapper.toEntity(domain)).thenReturn(entity);
            when(jpaPersonRepository.save(entity)).thenReturn(entity);
            when(personMapper.toDomain(entity)).thenReturn(domain);

            PersonDomain saved = adapter.save(domain);

            assertNotNull(saved);
            assertEquals(2L, saved.getId());
            assertEquals("ANA", saved.getName());
            verify(jpaPersonRepository, times(1)).save(entity);
        }

        @Test
        @DisplayName("Should find person by id in PersonPersistenceAdapter")
        void shouldFindByIdSuccessfully() {
            PersonEntity entity = createSampleEntity();
            PersonDomain domain = createSampleDomain();

            when(jpaPersonRepository.findById(2L)).thenReturn(Optional.of(entity));
            when(personMapper.toDomain(entity)).thenReturn(domain);

            Optional<PersonDomain> result = adapter.findById(2L);

            assertTrue(result.isPresent());
            assertEquals(2L, result.get().getId());
        }

        @Test
        @DisplayName("Should find person by numberid in PersonPersistenceAdapter")
        void shouldFindByNumberidSuccessfully() {
            PersonEntity entity = createSampleEntity();
            PersonDomain domain = createSampleDomain();

            when(jpaPersonRepository.findByNumberId("87654321")).thenReturn(Optional.of(entity));
            when(personMapper.toDomain(entity)).thenReturn(domain);

            Optional<PersonDomain> result = adapter.findByNumberid("87654321");

            assertTrue(result.isPresent());
            assertEquals("87654321", result.get().getNumberid());
        }
    }
}

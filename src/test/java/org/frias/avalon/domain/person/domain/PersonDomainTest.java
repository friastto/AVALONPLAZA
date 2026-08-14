package org.frias.avalon.domain.person.domain;

import org.frias.avalon.domain.person.domain.model.PersonDomain;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit Tests for PersonDomain Aggregate Root")
class PersonDomainTest {

    @Test
    @DisplayName("Should create PersonDomain model correctly")
    void shouldCreatePersonDomain() {
        PersonDomain person = PersonDomain.createBasic(
                1L,
                "12345678",
                "Juan",
                "Perez",
                "Av. Siempre Viva 123",
                1L,
                987654321L,
                "juan.perez@example.com",
                1L
        );

        assertNotNull(person);
        assertEquals("JUAN", person.getName());
        assertEquals("PEREZ", person.getLastName());
        assertEquals("JUAN PEREZ", person.getFullName());
        assertEquals("12345678", person.getNumberid());
        assertEquals("juan.perez@example.com", person.getEmail());
    }
}

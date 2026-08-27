package org.frias.avalon.domain.person.infraestructure.persistence.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit Tests for PersonEntity")
class PersonEntityTest {

    @Test
    @DisplayName("Should build PersonEntity with all fields correctly using builder and getters/setters")
    void shouldBuildPersonEntityCorrectly() {
        LocalDateTime now = LocalDateTime.now();
        PersonEntity entity = PersonEntity.builder()
                .id(100L)
                .numberId("99887766")
                .name("LAURA")
                .lastName("MARTINEZ")
                .address("CARRERA 7")
                .identificationId(1L)
                .sexId(2L)
                .phoneNumber(5559900L)
                .email("laura@email.com")
                .statusId(5L)
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertEquals(100L, entity.getId());
        assertEquals("99887766", entity.getNumberId());
        assertEquals("LAURA", entity.getName());
        assertEquals("MARTINEZ", entity.getLastName());
        assertEquals("CARRERA 7", entity.getAddress());
        assertEquals(1L, entity.getIdentificationId());
        assertEquals(2L, entity.getSexId());
        assertEquals(5559900L, entity.getPhoneNumber());
        assertEquals("laura@email.com", entity.getEmail());
        assertEquals(5L, entity.getStatusId());
        assertEquals(now, entity.getCreatedAt());
        assertEquals(now, entity.getUpdatedAt());

        // Test setters
        entity.setName("LAURA SOFIA");
        assertEquals("LAURA SOFIA", entity.getName());

        entity.setNumberId("11223344");
        assertEquals("11223344", entity.getNumberId());

        entity.setLastName("GOMEZ");
        assertEquals("GOMEZ", entity.getLastName());

        entity.setAddress("NUEVA DIRECCION");
        assertEquals("NUEVA DIRECCION", entity.getAddress());

        entity.setIdentificationId(3L);
        assertEquals(3L, entity.getIdentificationId());

        entity.setSexId(1L);
        assertEquals(1L, entity.getSexId());

        entity.setPhoneNumber(1234567L);
        assertEquals(1234567L, entity.getPhoneNumber());

        entity.setEmail("laurasofia@email.com");
        assertEquals("laurasofia@email.com", entity.getEmail());

        entity.setStatusId(6L);
        assertEquals(6L, entity.getStatusId());

        LocalDateTime later = now.plusDays(1);
        entity.setCreatedAt(later);
        entity.setUpdatedAt(later);
        assertEquals(later, entity.getCreatedAt());
        assertEquals(later, entity.getUpdatedAt());
    }

    @Test
    @DisplayName("Should test no-args constructor")
    void shouldCreateEmptyPersonEntity() {
        PersonEntity entity = new PersonEntity();
        assertNull(entity.getId());
        assertNull(entity.getName());
    }
}

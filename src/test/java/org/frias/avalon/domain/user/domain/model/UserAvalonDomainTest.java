package org.frias.avalon.domain.user.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas Unitarias - UserAvalonDomain")
class UserAvalonDomainTest {

    @Test
    @DisplayName("Debería crear un nuevo usuario mediante create()")
    void shouldCreateNewUser() {
        // Arrange
        String userName = "testuser";
        String hashSalt = "randomSalt123";
        String hashPassword = "hashedPassword456";
        Long statusId = 10L;

        // Act
        UserAvalonDomain user = UserAvalonDomain.create(userName, hashSalt, hashPassword, statusId);

        // Assert
        assertNull(user.getId(), "El ID debe ser nulo para un usuario recién creado");
        assertNull(user.getPersonId(), "El personId debe ser nulo inicialmente");
        assertEquals(userName, user.getUserName());
        assertEquals(hashSalt, user.getHashSalt());
        assertEquals(hashPassword, user.getHashPassword());
        assertEquals(statusId, user.getStatusId());
    }

    @Test
    @DisplayName("Debería restaurar un usuario básico desde persistencia mediante fromPersistenceBasic()")
    void shouldRestoreBasicUserFromPersistence() {
        // Arrange
        Long id = 1L;
        Long personId = 2L;
        String userName = "basicuser";
        Long statusId = 20L;

        // Act
        UserAvalonDomain user = UserAvalonDomain.fromPersistenceBasic(id, personId, userName, statusId);

        // Assert
        assertEquals(id, user.getId());
        assertEquals(personId, user.getPersonId());
        assertEquals(userName, user.getUserName());
        assertEquals(statusId, user.getStatusId());
        
        // Campos avanzados deberían ser nulos en la hidratación básica
        assertNull(user.getHashSalt());
        assertNull(user.getHashPassword());
    }

    @Test
    @DisplayName("Debería restaurar un usuario completo desde persistencia mediante fromPersistenceAdvanced()")
    void shouldRestoreAdvancedUserFromPersistence() {
        // Arrange
        Long id = 1L;
        Long personId = 2L;
        String userName = "advanceduser";
        String hashSalt = "salt789";
        String hashPassword = "hash789";
        Long statusId = 30L;

        // Act
        UserAvalonDomain user = UserAvalonDomain.fromPersistenceAdvanced(id, personId, userName, hashSalt, hashPassword, statusId);

        // Assert
        assertEquals(id, user.getId());
        assertEquals(personId, user.getPersonId());
        assertEquals(userName, user.getUserName());
        assertEquals(hashSalt, user.getHashSalt());
        assertEquals(hashPassword, user.getHashPassword());
        assertEquals(statusId, user.getStatusId());
    }
}

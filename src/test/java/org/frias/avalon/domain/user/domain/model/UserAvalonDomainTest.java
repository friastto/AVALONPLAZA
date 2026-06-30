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

    @Test
    @DisplayName("Debería cambiar el estado correctamente si el nuevo estado es diferente y no nulo")
    void shouldChangeStatusSuccessfully() {
        // Arrange
        UserAvalonDomain user = UserAvalonDomain.create("username", "salt", "pwd", 1L);

        // Act
        user.changeStatus(2L);

        // Assert
        assertEquals(2L, user.getStatusId());
    }

    @Test
    @DisplayName("Debería lanzar excepción si se intenta cambiar a un estado nulo")
    void shouldThrowExceptionWhenChangingStatusToNull() {
        // Arrange
        UserAvalonDomain user = UserAvalonDomain.create("username", "salt", "pwd", 1L);

        // Act & Assert
        Exception exception = assertThrows(org.frias.avalon.core.exeptions.DomainValidationException.class, () -> {
            user.changeStatus(null);
        });
        assertEquals("El nuevo estado no puede ser nulo.", exception.getMessage());
    }

    @Test
    @DisplayName("Debería lanzar excepción si se intenta cambiar al mismo estado actual")
    void shouldThrowExceptionWhenChangingToSameStatus() {
        // Arrange
        UserAvalonDomain user = UserAvalonDomain.create("username", "salt", "pwd", 1L);

        // Act & Assert
        Exception exception = assertThrows(org.frias.avalon.core.exeptions.DomainValidationException.class, () -> {
            user.changeStatus(1L);
        });
        assertEquals("El usuario ya se encuentra en el estado indicado.", exception.getMessage());
    }
}

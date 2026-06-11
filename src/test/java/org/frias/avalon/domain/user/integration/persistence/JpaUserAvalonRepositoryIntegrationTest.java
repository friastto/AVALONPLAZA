package org.frias.avalon.domain.user.integration.persistence;

import org.frias.avalon.domain.person.infraestructure.persistence.entity.PersonEntity;
import org.frias.avalon.domain.user.infraestructure.persistence.entity.UserAvalon;
import org.frias.avalon.domain.user.infraestructure.persistence.repository.JpaUserAvalonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test") // Usamos el perfil de test, que debería apuntar a tu BD de pruebas (PostgreSQL)
// CRÍTICO: Le dice a Spring que NO reemplace tu base de datos configurada (PostgreSQL) por H2
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("Pruebas de Integración - JpaUserAvalonRepository (BD Real)")
class JpaUserAvalonRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private JpaUserAvalonRepository jpaUserAvalonRepository;

    private UserAvalon testUser;

    @BeforeEach
    void setUp() {
        // Arrange: Insertamos datos directamente en la base de datos real.
        // Como @DataJpaTest incluye @Transactional, estos datos serán revertidos al final de la prueba.
        PersonEntity testPerson = new PersonEntity();
        testPerson.setNumberId("12345678");
        testPerson.setName("Test");
        testPerson.setLastName("User");
        testPerson.setEmail("test.user@example.com");
        testPerson.setIdentificationId(1L);
        testPerson.setStatusId(1L);
        testPerson.setCreatedAt(LocalDateTime.now());
        entityManager.persist(testPerson);

        testUser = new UserAvalon();
        testUser.setUserName("testuser");
        testUser.setHashPassword("hashedpassword");
        testUser.setHashSalt("salt");
        testUser.setStatusId(1L);
        testUser.setPersonId(testPerson.getId());
        testUser.setCreatedAt(LocalDateTime.now());
        entityManager.persist(testUser);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("Debería encontrar un usuario por su userName")
    void shouldFindByUserName() {
        // Act
        Optional<UserAvalon> foundUser = jpaUserAvalonRepository.findByUserName("testuser");

        // Assert
        assertTrue(foundUser.isPresent());
        assertEquals("testuser", foundUser.get().getUserName());
    }

    @Test
    @DisplayName("Debería encontrar un usuario por el numberId de su persona asociada")
    void shouldFindByPersonNumberid() {
        // Act
        Optional<UserAvalon> foundUser = jpaUserAvalonRepository.findByPersonNumberid("12345678");

        // Assert
        assertTrue(foundUser.isPresent());
        assertEquals(testUser.getId(), foundUser.get().getId());
    }

    @Test
    @DisplayName("Debería encontrar un usuario por su identificador (userName)")
    void shouldFindByIdentifier_withUserName() {
        // Act
        Optional<UserAvalon> foundUser = jpaUserAvalonRepository.findByIdentifier("testuser");

        // Assert
        assertTrue(foundUser.isPresent());
        assertEquals(testUser.getId(), foundUser.get().getId());
    }

    @Test
    @DisplayName("Debería encontrar un usuario por su identificador (email)")
    void shouldFindByIdentifier_withEmail() {
        // Act
        Optional<UserAvalon> foundUser = jpaUserAvalonRepository.findByIdentifier("test.user@example.com");

        // Assert
        assertTrue(foundUser.isPresent());
        assertEquals(testUser.getId(), foundUser.get().getId());
    }

    @Test
    @DisplayName("Debería encontrar un usuario por su identificador (numberId)")
    void shouldFindByIdentifier_withNumberId() {
        // Act
        Optional<UserAvalon> foundUser = jpaUserAvalonRepository.findByIdentifier("12345678");

        // Assert
        assertTrue(foundUser.isPresent());
        assertEquals(testUser.getId(), foundUser.get().getId());
    }
}

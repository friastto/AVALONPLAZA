package org.frias.avalon.domain.user.integration.persistence;

import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.masterdata.infraestructure.mapper.MasterDataMapperServiceImpl;
import org.frias.avalon.domain.person.infraestructure.mapper.PersonMapper;
import org.frias.avalon.domain.person.infraestructure.persistence.adapter.PersonPersistenceAdapter;
import org.frias.avalon.domain.person.infraestructure.persistence.entity.PersonEntity;
import org.frias.avalon.domain.user.domain.mapper.UserAvalonMapper;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.frias.avalon.domain.user.infraestructure.persistence.adapter.UserAvalonRepositoryAdapter;
import org.frias.avalon.domain.user.infraestructure.persistence.entity.UserAvalon;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
    UserAvalonRepositoryAdapter.class,
    UserAvalonMapper.class,
    PersonPersistenceAdapter.class,
    PersonMapper.class,
    MasterDataMapperServiceImpl.class,
    MasterTreeProvider.class
})
@Sql("/data-test.sql")
@DisplayName("Pruebas de Integración - UserAvalonRepositoryAdapter (BD Real)")
class UserAvalonRepositoryAdapterIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserAvalonRepositoryAdapter userAvalonRepositoryAdapter;

    @BeforeEach
    void setUp() {
        PersonEntity testPerson = new PersonEntity();
        testPerson.setNumberId("12345678");
        testPerson.setName("Test");
        testPerson.setLastName("User");
        testPerson.setEmail("test.user@example.com");
        testPerson.setIdentificationId(1L);
        testPerson.setStatusId(1L);
        testPerson.setCreatedAt(LocalDateTime.now());
        entityManager.persist(testPerson);

        UserAvalon testUser = new UserAvalon();
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
    @DisplayName("Debería encontrar un UserAvalonDomain por su userName")
    void shouldFindDomainByUserName() {
        // Act
        Optional<UserAvalonDomain> foundDomain = userAvalonRepositoryAdapter.findByUserName("testuser");

        // Assert
        assertTrue(foundDomain.isPresent());
        assertEquals("testuser", foundDomain.get().getUserName());
    }

    @Test
    @DisplayName("Debería encontrar un UserAvalonDomain por su identificador (email)")
    void shouldFindDomainByIdentifier() {
        // Act
        Optional<UserAvalonDomain> foundDomain = userAvalonRepositoryAdapter.findByIdentifier("test.user@example.com");

        // Assert
        assertTrue(foundDomain.isPresent());
        assertEquals("testuser", foundDomain.get().getUserName());
        assertNotNull(foundDomain.get().getHashPassword());
    }

    @Test
    @DisplayName("Debería guardar un nuevo UserAvalonDomain")
    void shouldSaveNewUserDomain() {
        // Arrange
        UserAvalonDomain newDomain = UserAvalonDomain.create("newuser", "newSalt", "newHash", 1L);

        // Act
        UserAvalonDomain savedDomain = userAvalonRepositoryAdapter.save(newDomain);

        // Assert
        assertNotNull(savedDomain.getId());
        assertEquals("newuser", savedDomain.getUserName());

        UserAvalon foundEntity = entityManager.find(UserAvalon.class, savedDomain.getId());
        assertNotNull(foundEntity);
        assertEquals("newuser", foundEntity.getUserName());
    }
}

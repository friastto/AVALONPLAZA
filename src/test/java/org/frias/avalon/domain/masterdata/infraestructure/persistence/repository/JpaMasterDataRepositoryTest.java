package org.frias.avalon.domain.masterdata.infraestructure.persistence.repository;

import lombok.AllArgsConstructor;
import org.frias.avalon.domain.masterdata.infraestructure.persistence.entity.MasterData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql("/data-test.sql")
class JpaMasterDataRepositoryTest {
@Autowired
    private JpaMasterDataRepository jpaMasterDataRepository;

    @Test
    @DisplayName("Debería encontrar una entidad por su shortName ('ACT')")
    void shouldFindByShortName() {
        // Act
        Optional<MasterData> result = jpaMasterDataRepository.findByShortName("ACT");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("ACTIVO", result.get().getFullName());
    }

    @Test
    @DisplayName("Debería retornar Optional.empty() si el shortName no existe")
    void shouldReturnEmptyWhenShortNameDoesNotExist() {
        // Act
        Optional<MasterData> result = jpaMasterDataRepository.findByShortName("NON_EXISTENT");

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Debería encontrar el ID de una entidad por su shortName ('GEN')")
    void shouldFindIdByShortName() {
        // Arrange
        MasterData genEntity = jpaMasterDataRepository.findByShortName("GEN").orElseThrow();

        // Act
        Long foundId = jpaMasterDataRepository.findIdByShortName("GEN");

        // Assert
        assertNotNull(foundId);
        assertEquals(genEntity.getId(), foundId);
    }

    @Test
    @DisplayName("Debería retornar nulo si no encuentra el ID para un shortName inexistente")
    void shouldReturnNullWhenIdByShortNameDoesNotExist() {
        // Act
        Long foundId = jpaMasterDataRepository.findIdByShortName("NON_EXISTENT");

        // Assert
        assertNull(foundId);
    }

    @Test
    @DisplayName("Debería encontrar el shortName de una entidad por su ID")
    void shouldFindShortNameById() {
        // Arrange
        MasterData actEntity = jpaMasterDataRepository.findByShortName("ACT").orElseThrow();

        // Act
        String shortName = jpaMasterDataRepository.findShortNameById(actEntity.getId());

        // Assert
        assertEquals("ACT", shortName);
    }

    @Test
    @DisplayName("Debería retornar nulo si no encuentra el shortName para un ID inexistente")
    void shouldReturnNullWhenShortNameByIdDoesNotExist() {
        // Act
        String shortName = jpaMasterDataRepository.findShortNameById(99999L);

        // Assert
        assertNull(shortName);
    }

    @Test
    @DisplayName("Debería encontrar la entidad padre dado el ID del hijo ('M' -> 'GEN')")
    void shouldFindParentByChildId() {
        // Arrange
        MasterData child = jpaMasterDataRepository.findByShortName("M").orElseThrow();

        // Act
        Optional<MasterData> parent = jpaMasterDataRepository.findParentByChildId(child.getId());

        // Assert
        assertTrue(parent.isPresent());
        assertEquals("GEN", parent.get().getShortName());
        assertEquals("TYPE_GENERO", parent.get().getFullName());
    }

    @Test
    @DisplayName("Debería retornar Optional.empty() si el nodo no tiene padre ('ROOTSTS')")
    void shouldReturnEmptyWhenFindingParentOfRootNode() {
        // Arrange
        MasterData rootNode = jpaMasterDataRepository.findByShortName("ROOTSTS").orElseThrow();

        // Act
        Optional<MasterData> parent = jpaMasterDataRepository.findParentByChildId(rootNode.getId());

        // Assert
        assertTrue(parent.isEmpty());
    }
}

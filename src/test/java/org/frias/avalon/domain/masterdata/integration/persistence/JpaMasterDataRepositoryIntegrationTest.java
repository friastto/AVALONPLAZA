package org.frias.avalon.domain.masterdata.integration.persistence;

import lombok.AllArgsConstructor;
import org.frias.avalon.domain.masterdata.infraestructure.persistence.entity.MasterData;
import org.frias.avalon.domain.masterdata.infraestructure.persistence.repository.JpaMasterDataRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")

@AllArgsConstructor(onConstructor_ = {@Autowired})
@DisplayName("Pruebas de Integración - Capa de Persistencia (JpaMasterDataRepository)")
class JpaMasterDataRepositoryIntegrationTest {

    private final JpaMasterDataRepository jpaMasterDataRepository;

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
        Long foundId = jpaMasterDataRepository.findByShortName("GEN").map(MasterData::getId).orElse(null);

        // Assert
        assertNotNull(foundId);
        assertEquals(genEntity.getId(), foundId);
    }

    @Test
    @DisplayName("Debería encontrar el shortName de una entidad por su ID")
    void shouldFindShortNameById() {
        // Arrange
        MasterData actEntity = jpaMasterDataRepository.findByShortName("ACT").orElseThrow();

        // Act
        String shortName = jpaMasterDataRepository.findById(actEntity.getId()).map(MasterData::getShortName).orElse(null);

        // Assert
        assertEquals("ACT", shortName);
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
}

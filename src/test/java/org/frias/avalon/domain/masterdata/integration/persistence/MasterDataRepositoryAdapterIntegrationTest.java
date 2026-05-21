package org.frias.avalon.domain.masterdata.integration.persistence;

import lombok.AllArgsConstructor;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.infraestructure.mapper.MasterDataMapperServiceImpl;
import org.frias.avalon.domain.masterdata.infraestructure.persistence.adapter.MasterDataRepositoryAdapter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import({MasterDataMapperServiceImpl.class, MasterDataRepositoryAdapter.class})
@Sql("/data-test.sql")

@DisplayName("Pruebas de Integración - Capa de Adaptador de Persistencia")
class MasterDataRepositoryAdapterIntegrationTest {

    @Autowired
    private  MasterDataRepositoryAdapter masterDataRepositoryAdapter;

    @Test
    @DisplayName("Debería guardar un nuevo MasterRoot y retornarlo con su ID")
    void shouldSaveMasterRootAndReturnWithId() {
        // Arrange
        Long parentId = masterDataRepositoryAdapter.getIdByCode("GEN");
        Long statusId = masterDataRepositoryAdapter.getIdByCode("ACT");
        assertNotNull(parentId, "El ID para 'GEN' no debería ser nulo");
        assertNotNull(statusId, "El ID para 'ACT' no debería ser nulo");

        MasterRoot newDomain = MasterRoot.create("NEW_CODE", "New Code Full Name", parentId, statusId);

        // Act
        MasterRoot savedDomain = masterDataRepositoryAdapter.save(newDomain);

        // Assert
        assertNotNull(savedDomain.getId());
        assertEquals("NEW_CODE", savedDomain.getShortName());

        Optional<MasterRoot> foundInDb = masterDataRepositoryAdapter.findById(savedDomain.getId());
        assertTrue(foundInDb.isPresent());
        assertEquals("NEW_CODE", foundInDb.get().getShortName());
    }

    @Test
    @DisplayName("Debería encontrar un MasterRoot por su shortName (código)")
    void shouldFindMasterRootByCode() {
        // Act
        Optional<MasterRoot> foundDomain = masterDataRepositoryAdapter.findByCode("GEN");

        // Assert
        assertTrue(foundDomain.isPresent());
        assertEquals("GEN", foundDomain.get().getShortName());
        assertEquals("TYPE_GENERO", foundDomain.get().getFullName());
    }

    @Test
    @DisplayName("Debería encontrar el padre 'GEN' del nodo hijo 'M'")
    void shouldFindParentByChildId() {
        // Arrange
        Long childId = masterDataRepositoryAdapter.getIdByCode("M");
        assertNotNull(childId, "El ID para 'M' no debería ser nulo");

        // Act
        Optional<MasterRoot> foundParent = masterDataRepositoryAdapter.findParentByChildrenId(childId);

        // Assert
        assertTrue(foundParent.isPresent());
        assertEquals("GEN", foundParent.get().getShortName());
    }
}

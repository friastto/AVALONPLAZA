package org.frias.avalon.domain.masterdata.infraestructure.persistence.adapter;

import lombok.AllArgsConstructor;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.infraestructure.mapper.MasterDataMapperServiceImpl;
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
@AllArgsConstructor(onConstructor_ = {@Autowired})
class MasterDataRepositoryAdapterTest {

    private final MasterDataRepositoryAdapter masterDataRepositoryAdapter;

    @Test
    @DisplayName("Debería guardar un nuevo MasterRoot y retornarlo con su ID")
    void shouldSaveMasterRootAndReturnWithId() {
        // Arrange: Obtenemos IDs de datos pre-cargados para la asociación
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

        // Verificación directa en la BD
        Optional<MasterRoot> foundInDb = masterDataRepositoryAdapter.findById(savedDomain.getId());
        assertTrue(foundInDb.isPresent());
        assertEquals("NEW_CODE", foundInDb.get().getShortName());
    }

    @Test
    @DisplayName("Debería encontrar un MasterRoot por su shortName (código) usando datos de data-test.sql")
    void shouldFindMasterRootByCode() {
        // Act
        Optional<MasterRoot> foundDomain = masterDataRepositoryAdapter.findByCode("GEN");

        // Assert
        assertTrue(foundDomain.isPresent());
        assertEquals("GEN", foundDomain.get().getShortName());
        assertEquals("TYPE_GENERO", foundDomain.get().getFullName());
    }

    @Test
    @DisplayName("Debería obtener el ID de un MasterRoot por su shortName (código)")
    void shouldGetIdByCode() {
        // Act
        Optional<MasterRoot> foundDomain = masterDataRepositoryAdapter.findByCode("GEN");
        assertTrue(foundDomain.isPresent(), "El código 'GEN' debe existir en data-test.sql");
        Long expectedId = foundDomain.get().getId();

        Long foundId = masterDataRepositoryAdapter.getIdByCode("GEN");

        // Assert
        assertEquals(expectedId, foundId);
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

    @Test
    @DisplayName("Debería retornar Optional.empty() al buscar el padre de un nodo raíz ('GEN')")
    void shouldReturnEmptyWhenNodeHasNoParent() {
        // Arrange
        Long rootId = masterDataRepositoryAdapter.getIdByCode("GEN");
        assertNotNull(rootId, "El ID para 'GEN' no debería ser nulo");

        // Act
        Optional<MasterRoot> foundParent = masterDataRepositoryAdapter.findParentByChildrenId(rootId);

        // Assert
        assertTrue(foundParent.isEmpty());
    }
    
    @Test
    @DisplayName("Debería encontrar el estado activo 'ACT'")
    void shouldGetActiveStatus() {
        // Act
        Optional<MasterRoot> activeStatus = masterDataRepositoryAdapter.getActiveStatus();
        
        // Assert
        assertTrue(activeStatus.isPresent());
        assertEquals("ACT", activeStatus.get().getShortName());
    }

    @Test
    @DisplayName("Debería lanzar DomainValidationException al intentar eliminar un nodo con hijos")
    void shouldThrowDomainValidationExceptionWhenDeletingNodeWithChildren() {
        // Arrange
        Long parentId = masterDataRepositoryAdapter.getIdByCode("GEN");
        assertNotNull(parentId);

        // Act & Assert
        org.frias.avalon.core.exeptions.DomainValidationException exception = assertThrows(
                org.frias.avalon.core.exeptions.DomainValidationException.class,
                () -> masterDataRepositoryAdapter.deleteById(parentId)
        );
        assertEquals("No se puede eliminar el nodo porque contiene subcategorias o ramas fijadas.", exception.getMessage());
    }

    @Test
    @DisplayName("Debería actualizar el parentId correctamente")
    void shouldUpdateParentIdSuccessfully() {
        // Arrange
        Long childId = masterDataRepositoryAdapter.getIdByCode("M");
        Long newParentId = masterDataRepositoryAdapter.getIdByCode("IDENT");
        assertNotNull(childId);
        assertNotNull(newParentId);

        // Act
        MasterRoot updated = masterDataRepositoryAdapter.updateParentId(childId, newParentId);

        // Assert
        assertNotNull(updated);
        assertEquals(newParentId, updated.getParentId());
    }
}

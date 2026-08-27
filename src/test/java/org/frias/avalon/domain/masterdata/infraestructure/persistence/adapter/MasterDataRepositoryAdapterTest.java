package org.frias.avalon.domain.masterdata.infraestructure.persistence.adapter;

import jakarta.persistence.EntityNotFoundException;
import org.frias.avalon.core.exeptions.DomainValidationException;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.infraestructure.mapper.MasterDataMapperService;
import org.frias.avalon.domain.masterdata.infraestructure.persistence.entity.MasterData;
import org.frias.avalon.domain.masterdata.infraestructure.persistence.repository.JpaMasterDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias integrales para MasterDataRepositoryAdapter utilizando JUnit 5 y Mockito.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias para MasterDataRepositoryAdapter - Adaptador de Persistencia")
class MasterDataRepositoryAdapterTest {

    @Mock
    private JpaMasterDataRepository jpa;

    @Mock
    private MasterDataMapperService mapper;

    @InjectMocks
    private MasterDataRepositoryAdapter masterDataRepositoryAdapter;

    private MasterData entity;
    private MasterRoot domain;

    @BeforeEach
    void setUp() {
        entity = MasterData.builder()
                .id(1L)
                .shortName("ACT")
                .fullName("Activo")
                .parentId(null)
                .statusId(1L)
                .build();

        domain = new MasterRoot(1L, "ACT", "Activo", null, 1L);
    }

    @Nested
    @DisplayName("Búsquedas por ID y Código")
    class FindTests {

        @Test
        @DisplayName("findById debería retornar Optional<MasterRoot> cuando la entidad existe")
        void shouldFindByIdSuccessfully() {
            // Arrange
            when(jpa.findById(1L)).thenReturn(Optional.of(entity));
            when(mapper.toDomain(entity)).thenReturn(domain);

            // Act
            Optional<MasterRoot> result = masterDataRepositoryAdapter.findById(1L);

            // Assert
            assertTrue(result.isPresent());
            assertEquals(domain, result.get());
            verify(jpa).findById(1L);
            verify(mapper).toDomain(entity);
        }

        @Test
        @DisplayName("findById debería retornar Optional.empty() cuando la entidad no existe")
        void shouldReturnEmptyWhenFindByIdNotFound() {
            // Arrange
            when(jpa.findById(99L)).thenReturn(Optional.empty());

            // Act
            Optional<MasterRoot> result = masterDataRepositoryAdapter.findById(99L);

            // Assert
            assertTrue(result.isEmpty());
            verify(jpa).findById(99L);
            verifyNoInteractions(mapper);
        }

        @Test
        @DisplayName("findByCode debería retornar Optional<MasterRoot> cuando existe el shortName")
        void shouldFindByCodeSuccessfully() {
            // Arrange
            when(jpa.findByShortName("ACT")).thenReturn(Optional.of(entity));
            when(mapper.toDomain(entity)).thenReturn(domain);

            // Act
            Optional<MasterRoot> result = masterDataRepositoryAdapter.findByCode("ACT");

            // Assert
            assertTrue(result.isPresent());
            assertEquals("ACT", result.get().getShortName());
            verify(jpa).findByShortName("ACT");
            verify(mapper).toDomain(entity);
        }

        @Test
        @DisplayName("findByCode debería retornar Optional.empty() si el shortName no existe")
        void shouldReturnEmptyWhenFindByCodeNotFound() {
            // Arrange
            when(jpa.findByShortName("UNKNOWN")).thenReturn(Optional.empty());

            // Act
            Optional<MasterRoot> result = masterDataRepositoryAdapter.findByCode("UNKNOWN");

            // Assert
            assertTrue(result.isEmpty());
            verify(jpa).findByShortName("UNKNOWN");
        }

        @Test
        @DisplayName("getIdByCode debería retornar el ID correspondiente al código")
        void shouldGetIdByCode() {
            // Arrange
            when(jpa.findIdByShortName("ACT")).thenReturn(10L);

            // Act
            Long id = masterDataRepositoryAdapter.getIdByCode("ACT");

            // Assert
            assertEquals(10L, id);
            verify(jpa).findIdByShortName("ACT");
        }

        @Test
        @DisplayName("getCodeById debería retornar el shortName correspondiente al ID")
        void shouldGetCodeById() {
            // Arrange
            when(jpa.findShortNameById(10L)).thenReturn("ACT");

            // Act
            String code = masterDataRepositoryAdapter.getCodeById(10L);

            // Assert
            assertEquals("ACT", code);
            verify(jpa).findShortNameById(10L);
        }

        @Test
        @DisplayName("getActiveStatus debería retornar el estado activo 'ACT'")
        void shouldGetActiveStatus() {
            // Arrange
            when(jpa.findByShortName("ACT")).thenReturn(Optional.of(entity));
            when(mapper.toDomain(entity)).thenReturn(domain);

            // Act
            Optional<MasterRoot> activeStatus = masterDataRepositoryAdapter.getActiveStatus();

            // Assert
            assertTrue(activeStatus.isPresent());
            assertEquals("ACT", activeStatus.get().getShortName());
            verify(jpa).findByShortName("ACT");
        }
    }

    @Nested
    @DisplayName("Operaciones de Guardado y Modificación")
    class SaveAndUpdateTests {

        @Test
        @DisplayName("save debería mapear a entidad, guardar y retornar objeto de dominio")
        void shouldSaveMasterRootSuccessfully() {
            // Arrange
            MasterRoot newDomain = MasterRoot.create("NEW", "Nuevo", null, 1L);
            MasterData newEntity = MasterData.builder().shortName("NEW").fullName("NUEVO").statusId(1L).build();
            MasterData savedEntity = MasterData.builder().id(2L).shortName("NEW").fullName("NUEVO").statusId(1L).build();
            MasterRoot savedDomain = new MasterRoot(2L, "NEW", "NUEVO", null, 1L);

            when(mapper.toEntity(newDomain)).thenReturn(newEntity);
            when(jpa.save(newEntity)).thenReturn(savedEntity);
            when(mapper.toDomain(savedEntity)).thenReturn(savedDomain);

            // Act
            MasterRoot result = masterDataRepositoryAdapter.save(newDomain);

            // Assert
            assertNotNull(result);
            assertEquals(2L, result.getId());
            assertEquals("NEW", result.getShortName());

            verify(mapper).toEntity(newDomain);
            verify(jpa).save(newEntity);
            verify(mapper).toDomain(savedEntity);
        }

        @Test
        @DisplayName("saveAll debería guardar una lista de objetos de dominio")
        void shouldSaveAllMasterRootsSuccessfully() {
            // Arrange
            List<MasterRoot> domainList = List.of(domain);
            List<MasterData> entityList = List.of(entity);

            when(mapper.toEntity(domain)).thenReturn(entity);
            when(jpa.saveAll(anyList())).thenReturn(entityList);
            when(mapper.toDomain(entity)).thenReturn(domain);

            // Act
            List<MasterRoot> savedList = masterDataRepositoryAdapter.saveAll(domainList);

            // Assert
            assertNotNull(savedList);
            assertEquals(1, savedList.size());
            assertEquals(domain, savedList.get(0));

            verify(jpa).saveAll(anyList());
        }

        @Test
        @DisplayName("updateParentId debería actualizar exitosamente cuando el nuevo parentId existe")
        void shouldUpdateParentIdSuccessfully() {
            // Arrange
            Long nodeId = 1L;
            Long newParentId = 5L;

            when(jpa.findById(nodeId)).thenReturn(Optional.of(entity));
            when(jpa.existsById(newParentId)).thenReturn(true);
            when(jpa.save(entity)).thenReturn(entity);
            when(mapper.toDomain(entity)).thenReturn(new MasterRoot(1L, "ACT", "Activo", 5L, 1L));

            // Act
            MasterRoot updated = masterDataRepositoryAdapter.updateParentId(nodeId, newParentId);

            // Assert
            assertNotNull(updated);
            assertEquals(5L, updated.getParentId());
            verify(jpa).findById(nodeId);
            verify(jpa).existsById(newParentId);
            verify(jpa).save(entity);
        }

        @Test
        @DisplayName("updateParentId debería permitir establecer parentId a null")
        void shouldUpdateParentIdToNullSuccessfully() {
            // Arrange
            Long nodeId = 1L;
            entity.setParentId(5L);

            when(jpa.findById(nodeId)).thenReturn(Optional.of(entity));
            when(jpa.save(entity)).thenReturn(entity);
            when(mapper.toDomain(entity)).thenReturn(domain);

            // Act
            MasterRoot updated = masterDataRepositoryAdapter.updateParentId(nodeId, null);

            // Assert
            assertNotNull(updated);
            assertNull(updated.getParentId());
            verify(jpa).findById(nodeId);
            verify(jpa, never()).existsById(any());
            verify(jpa).save(entity);
        }

        @Test
        @DisplayName("updateParentId debería lanzar EntityNotFoundException si el nodo a actualizar no existe")
        void shouldThrowExceptionWhenUpdatingNonExistingNode() {
            // Arrange
            when(jpa.findById(99L)).thenReturn(Optional.empty());

            // Act & Assert
            EntityNotFoundException ex = assertThrows(
                    EntityNotFoundException.class,
                    () -> masterDataRepositoryAdapter.updateParentId(99L, 5L)
            );
            assertEquals("MasterData no encontrado con id: 99", ex.getMessage());
            verify(jpa, never()).save(any());
        }

        @Test
        @DisplayName("updateParentId debería lanzar DomainValidationException si el nuevo parentId no existe")
        void shouldThrowExceptionWhenNewParentIdDoesNotExist() {
            // Arrange
            Long nodeId = 1L;
            Long nonExistingParentId = 999L;

            when(jpa.findById(nodeId)).thenReturn(Optional.of(entity));
            when(jpa.existsById(nonExistingParentId)).thenReturn(false);

            // Act & Assert
            DomainValidationException ex = assertThrows(
                    DomainValidationException.class,
                    () -> masterDataRepositoryAdapter.updateParentId(nodeId, nonExistingParentId)
            );
            assertEquals("El nuevo parentId especificado no existe: 999", ex.getMessage());
            verify(jpa, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Operaciones de Eliminación")
    class DeleteTests {

        @Test
        @DisplayName("deleteById debería eliminar y retornar el nodo cuando no tiene hijos")
        void shouldDeleteByIdSuccessfully() {
            // Arrange
            Long idToDelete = 1L;
            when(jpa.existsByParentId(idToDelete)).thenReturn(false);
            when(jpa.findById(idToDelete)).thenReturn(Optional.of(entity));
            when(mapper.toDomain(entity)).thenReturn(domain);

            // Act
            MasterRoot deleted = masterDataRepositoryAdapter.deleteById(idToDelete);

            // Assert
            assertNotNull(deleted);
            assertEquals(idToDelete, deleted.getId());

            verify(jpa).existsByParentId(idToDelete);
            verify(jpa).findById(idToDelete);
            verify(jpa).delete(entity);
        }

        @Test
        @DisplayName("deleteById debería lanzar DomainValidationException si el nodo tiene hijos")
        void shouldThrowDomainValidationExceptionWhenDeletingNodeWithChildren() {
            // Arrange
            Long parentId = 1L;
            when(jpa.existsByParentId(parentId)).thenReturn(true);

            // Act & Assert
            DomainValidationException ex = assertThrows(
                    DomainValidationException.class,
                    () -> masterDataRepositoryAdapter.deleteById(parentId)
            );
            assertEquals("No se puede eliminar el nodo porque contiene subcategorias o ramas fijadas.", ex.getMessage());
            verify(jpa, never()).findById(any());
            verify(jpa, never()).delete(any());
        }

        @Test
        @DisplayName("deleteById debería lanzar EntityNotFoundException si el nodo no existe")
        void shouldThrowEntityNotFoundExceptionWhenDeletingNonExistingNode() {
            // Arrange
            Long nonExistingId = 99L;
            when(jpa.existsByParentId(nonExistingId)).thenReturn(false);
            when(jpa.findById(nonExistingId)).thenReturn(Optional.empty());

            // Act & Assert
            EntityNotFoundException ex = assertThrows(
                    EntityNotFoundException.class,
                    () -> masterDataRepositoryAdapter.deleteById(nonExistingId)
            );
            assertEquals("No se puede eliminar. No se encontro MasterData con id: 99", ex.getMessage());
            verify(jpa, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("Búsquedas Hierárquicas y Listados")
    class HierarchyAndListTests {

        @Test
        @DisplayName("findParentByChildrenId debería retornar el padre cuando existe")
        void shouldFindParentByChildrenIdSuccessfully() {
            // Arrange
            Long childId = 2L;
            MasterData parentEntity = MasterData.builder().id(1L).shortName("PARENT").fullName("Padre").build();
            MasterRoot parentDomain = new MasterRoot(1L, "PARENT", "Padre", null, 1L);

            when(jpa.findParentByChildId(childId)).thenReturn(Optional.of(parentEntity));
            when(mapper.toDomain(parentEntity)).thenReturn(parentDomain);

            // Act
            Optional<MasterRoot> result = masterDataRepositoryAdapter.findParentByChildrenId(childId);

            // Assert
            assertTrue(result.isPresent());
            assertEquals("PARENT", result.get().getShortName());
            verify(jpa).findParentByChildId(childId);
        }

        @Test
        @DisplayName("findParentByChildrenId debería retornar Optional.empty() si el nodo no tiene padre")
        void shouldReturnEmptyWhenChildHasNoParent() {
            // Arrange
            Long rootId = 1L;
            when(jpa.findParentByChildId(rootId)).thenReturn(Optional.empty());

            // Act
            Optional<MasterRoot> result = masterDataRepositoryAdapter.findParentByChildrenId(rootId);

            // Assert
            assertTrue(result.isEmpty());
            verify(jpa).findParentByChildId(rootId);
        }

        @Test
        @DisplayName("findAll debería retornar la lista completa de MasterRoot")
        void shouldFindAllSuccessfully() {
            // Arrange
            when(jpa.findAll()).thenReturn(List.of(entity));
            when(mapper.toDomain(entity)).thenReturn(domain);

            // Act
            List<MasterRoot> list = masterDataRepositoryAdapter.findAll();

            // Assert
            assertNotNull(list);
            assertEquals(1, list.size());
            assertEquals(domain, list.get(0));
            verify(jpa).findAll();
        }

        @Test
        @DisplayName("findChildrenByParentCode debería retornar la lista de hijos de un código padre")
        void shouldFindChildrenByParentCodeSuccessfully() {
            // Arrange
            String parentCode = "PARENT";
            when(jpa.findChildrenByParentCode(parentCode)).thenReturn(List.of(entity));
            when(mapper.toDomain(entity)).thenReturn(domain);

            // Act
            List<MasterRoot> children = masterDataRepositoryAdapter.findChildrenByParentCode(parentCode);

            // Assert
            assertNotNull(children);
            assertEquals(1, children.size());
            assertEquals(domain, children.get(0));
            verify(jpa).findChildrenByParentCode(parentCode);
        }
    }
}

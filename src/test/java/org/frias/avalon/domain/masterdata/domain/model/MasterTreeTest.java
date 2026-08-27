package org.frias.avalon.domain.masterdata.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias integrales para el modelo de dominio MasterTree.
 */
@DisplayName("Pruebas Unitarias para MasterTree - Modelo de Dominio")
class MasterTreeTest {

    private MasterTree masterTree;
    private MasterRoot rootNode;
    private MasterRoot childNode;
    private MasterRoot grandchildNode;
    private MasterRoot anotherRoot;
    private MasterRoot orphanNode;

    @BeforeEach
    void setUp() {
        // Jerarquía de nodos de prueba:
        // rootNode (ID=1, ROOT)
        //   └── childNode (ID=2, CHILD, parentId=1)
        //         └── grandchildNode (ID=3, GRANDCHILD, parentId=2)
        // anotherRoot (ID=4, OTHER, parentId=null)
        // orphanNode (ID=5, ORPHAN, parentId=999) [parentId no existe en el mapa]

        rootNode = new MasterRoot(1L, "ROOT", "Root Node", null, 1L);
        childNode = new MasterRoot(2L, "CHILD", "Child Node", 1L, 1L);
        grandchildNode = new MasterRoot(3L, "GRANDCHILD", "Grandchild Node", 2L, 1L);
        anotherRoot = new MasterRoot(4L, "OTHER", "Another Root", null, 1L);
        orphanNode = new MasterRoot(5L, "ORPHAN", "Orphan Node", 999L, 1L);

        List<MasterRoot> nodes = List.of(rootNode, childNode, grandchildNode, anotherRoot, orphanNode);
        masterTree = new MasterTree(nodes);
    }

    @Nested
    @DisplayName("Búsqueda por ID")
    class GetByIdTests {

        @Test
        @DisplayName("getById debería recuperar el nodo correspondiente al ID existente")
        void shouldGetNodeById() {
            assertEquals(rootNode, masterTree.getById(1L));
            assertEquals(childNode, masterTree.getById(2L));
            assertEquals(grandchildNode, masterTree.getById(3L));
        }

        @Test
        @DisplayName("getById debería retornar null cuando el ID no existe")
        void shouldReturnNullWhenIdNotFound() {
            assertNull(masterTree.getById(99L));
        }

        @Test
        @DisplayName("getByIdOrThrow debería retornar el nodo cuando el ID existe")
        void shouldReturnNodeWhenGetByIdOrThrowFound() {
            MasterRoot result = masterTree.getByIdOrThrow(1L);
            assertNotNull(result);
            assertEquals("ROOT", result.getShortName());
        }

        @Test
        @DisplayName("getByIdOrThrow debería lanzar IllegalStateException cuando el ID no existe")
        void shouldThrowExceptionWhenIdNotFound() {
            IllegalStateException ex = assertThrows(
                    IllegalStateException.class,
                    () -> masterTree.getByIdOrThrow(99L)
            );
            assertEquals("MasterData no encontrado: 99", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("Búsqueda por Código")
    class GetByCodeTests {

        @Test
        @DisplayName("getByCode debería retornar null si el código provisto es null")
        void shouldReturnNullWhenCodeIsNull() {
            assertNull(masterTree.getByCode(null));
        }

        @Test
        @DisplayName("getByCode debería recuperar el nodo ignorando diferencias de mayúsculas/minúsculas")
        void shouldGetNodeByCodeIgnoringCase() {
            assertEquals(rootNode, masterTree.getByCode("ROOT"));
            assertEquals(rootNode, masterTree.getByCode("root"));
            assertEquals(childNode, masterTree.getByCode("ChIlD"));
        }

        @Test
        @DisplayName("getByCode debería retornar null para un código inexistente")
        void shouldReturnNullWhenCodeNotFound() {
            assertNull(masterTree.getByCode("NON_EXISTENT"));
        }

        @Test
        @DisplayName("El constructor debe manejar duplicados de código reemplazando con el último elemento")
        void constructorShouldHandleDuplicateCodes() {
            MasterRoot nodeA = new MasterRoot(10L, "DUP", "First", null, 1L);
            MasterRoot nodeB = new MasterRoot(11L, "DUP", "Second", null, 1L);

            MasterTree treeWithDuplicates = new MasterTree(List.of(nodeA, nodeB));

            MasterRoot result = treeWithDuplicates.getByCode("DUP");
            assertNotNull(result);
            assertEquals(11L, result.getId());
            assertEquals("Second", result.getFullName());
        }
    }

    @Nested
    @DisplayName("Verificación de Jerarquía isChildOf")
    class IsChildOfTests {

        @Test
        @DisplayName("isChildOf debería retornar true para hijos directos")
        void shouldReturnTrueForDirectChild() {
            assertTrue(masterTree.isChildOf(childNode, "ROOT"));
        }

        @Test
        @DisplayName("isChildOf debería retornar true para descendientes indirectos (nietos)")
        void shouldReturnTrueForIndirectChild() {
            assertTrue(masterTree.isChildOf(grandchildNode, "ROOT"));
            assertTrue(masterTree.isChildOf(grandchildNode, "CHILD"));
        }

        @Test
        @DisplayName("isChildOf debería retornar false cuando el nodo es una raíz sin padre")
        void shouldReturnFalseForRootNode() {
            assertFalse(masterTree.isChildOf(rootNode, "ROOT"));
            assertFalse(masterTree.isChildOf(rootNode, "OTHER"));
        }

        @Test
        @DisplayName("isChildOf debería retornar false para nodos no relacionados")
        void shouldReturnFalseForUnrelatedNodes() {
            assertFalse(masterTree.isChildOf(anotherRoot, "ROOT"));
            assertFalse(masterTree.isChildOf(childNode, "OTHER"));
        }

        @Test
        @DisplayName("isChildOf debería retornar false si el parentId del nodo no existe en el árbol")
        void shouldReturnFalseWhenParentNotInTree() {
            assertFalse(masterTree.isChildOf(orphanNode, "ROOT"));
        }
    }

    @Nested
    @DisplayName("Evaluación de Coincidencia is e isAny")
    class MatcherTests {

        @Test
        @DisplayName("is debería retornar false si el nodo o el código son null")
        void isShouldReturnFalseOnNullInputs() {
            assertFalse(masterTree.is(null, "ROOT"));
            assertFalse(masterTree.is(rootNode, null));
            assertFalse(masterTree.is(null, null));
        }

        @Test
        @DisplayName("is debería comparar códigos ignorando mayúsculas/minúsculas y espacios")
        void isShouldCompareCodesIgnoringCaseAndSpaces() {
            assertTrue(masterTree.is(rootNode, "ROOT"));
            assertTrue(masterTree.is(rootNode, "root"));
            assertFalse(masterTree.is(rootNode, "CHILD"));
        }

        @Test
        @DisplayName("isAny debería retornar false si el nodo o el array de códigos es null")
        void isAnyShouldReturnFalseOnNullInputs() {
            assertFalse(masterTree.isAny(null, "ROOT", "CHILD"));
            assertFalse(masterTree.isAny(rootNode, (String[]) null));
        }

        @Test
        @DisplayName("isAny debería retornar true si el nodo coincide con al menos uno de los códigos")
        void isAnyShouldReturnTrueIfMatchesAnyCode() {
            assertTrue(masterTree.isAny(rootNode, "CODE1", "ROOT", "CODE2"));
            assertTrue(masterTree.isAny(childNode, "child", "OTHER"));
        }

        @Test
        @DisplayName("isAny debería retornar false si el nodo no coincide con ninguno de los códigos")
        void isAnyShouldReturnFalseIfNoMatches() {
            assertFalse(masterTree.isAny(rootNode, "CODE1", "CODE2"));
        }
    }
}

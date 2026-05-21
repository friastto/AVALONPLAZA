package org.frias.avalon.domain.masterdata.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MasterTreeTest {

    private MasterTree masterTree;
    private MasterRoot rootNode;
    private MasterRoot childNode;
    private MasterRoot grandchildNode;
    private MasterRoot anotherRoot;

    @BeforeEach
    void setUp() {
        // Arrange: Creamos una jerarquía de nodos de prueba
        rootNode = new MasterRoot(1L, "ROOT", "Root Node", null, 1L);
        childNode = new MasterRoot(2L, "CHILD", "Child Node", 1L, 1L);
        grandchildNode = new MasterRoot(3L, "GRANDCHILD", "Grandchild Node", 2L, 1L);
        anotherRoot = new MasterRoot(4L, "OTHER", "Another Root", null, 1L);

        List<MasterRoot> nodes = List.of(rootNode, childNode, grandchildNode, anotherRoot);
        masterTree = new MasterTree(nodes);
    }

    @Test
    @DisplayName("Debería recuperar nodos por ID correctamente")
    void shouldGetNodeById() {
        assertEquals(rootNode, masterTree.getById(1L));
        assertEquals(childNode, masterTree.getById(2L));
        assertNull(masterTree.getById(99L)); // ID no existente
    }

    @Test
    @DisplayName("Debería recuperar nodos por código (insensible a mayúsculas)")
    void shouldGetNodeByCode() {
        assertEquals(rootNode, masterTree.getByCode("ROOT"));
        assertEquals(childNode, masterTree.getByCode("child")); // Prueba de insensibilidad a mayúsculas
        assertNull(masterTree.getByCode("NON_EXISTENT"));
    }

    @Test
    @DisplayName("getByIdOrThrow debería lanzar excepción si el ID no existe")
    void shouldThrowExceptionWhenIdNotFound() {
        assertThrows(IllegalStateException.class, () -> {
            masterTree.getByIdOrThrow(99L);
        });
    }

    @Test
    @DisplayName("isChildOf debería retornar true para hijos directos e indirectos (nietos)")
    void isChildOfShouldReturnTrueForDirectAndIndirectChildren() {
        assertTrue(masterTree.isChildOf(childNode, "ROOT"));
        assertTrue(masterTree.isChildOf(grandchildNode, "ROOT"));
    }

    @Test
    @DisplayName("isChildOf debería retornar false para nodos no relacionados o para el mismo nodo")
    void isChildOfShouldReturnFalseForUnrelatedNodes() {
        assertFalse(masterTree.isChildOf(anotherRoot, "ROOT")); // Otro árbol
        assertFalse(masterTree.isChildOf(rootNode, "ROOT")); // No es hijo de sí mismo
        assertFalse(masterTree.isChildOf(childNode, "OTHER")); // Padre incorrecto
    }

    @Test
    @DisplayName("is() debería comparar códigos de forma insensible a mayúsculas")
    void isShouldCompareCodeIgnoringCase() {
        assertTrue(masterTree.is(rootNode, "root"));
        assertFalse(masterTree.is(rootNode, "CHILD"));
    }

    @Test
    @DisplayName("isAny() debería retornar true si el nodo coincide con alguno de los códigos")
    void isAnyShouldReturnTrueIfNodeMatchesAnyCode() {
        assertTrue(masterTree.isAny(rootNode, "CODE1", "ROOT", "CODE2"));
        assertFalse(masterTree.isAny(rootNode, "CODE1", "CODE2"));
    }
}

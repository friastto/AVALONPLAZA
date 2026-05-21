package org.frias.avalon.domain.masterdata.domain.service;

import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.repository.MasterDataRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MasterTreeProviderTest {

    @Mock
    private MasterDataRepositoryPort masterDataRepositoryPort;

    @InjectMocks
    private MasterTreeProvider masterTreeProvider;

    private List<MasterRoot> mockNodes;

    @BeforeEach
    void setUp() {
        MasterRoot rootNode = new MasterRoot(1L, "ROOT", "Root Node", null, 1L);
        MasterRoot childNode = new MasterRoot(2L, "CHILD", "Child Node", 1L, 1L);
        mockNodes = List.of(rootNode, childNode);
    }

    @Test
    @DisplayName("Debería inicializar el árbol correctamente al llamar a refresh()")
    void shouldInitializeTreeOnRefresh() {
        // Arrange
        when(masterDataRepositoryPort.findAll()).thenReturn(mockNodes);

        // Act
        masterTreeProvider.refresh();

        // Assert
        MasterTree tree = masterTreeProvider.getTree();
        assertNotNull(tree, "El árbol no debe ser nulo después del refresh");
        verify(masterDataRepositoryPort, times(1)).findAll();
        
        // Verificamos que el árbol contenga los nodos mockeados
        assertNotNull(tree.getByCode("ROOT"), "El nodo ROOT debería estar en el árbol");
        assertNotNull(tree.getByCode("CHILD"), "El nodo CHILD debería estar en el árbol");
    }

    @Test
    @DisplayName("init() debería llamar a refresh()")
    void initShouldCallRefresh() {
        // Arrange
        when(masterDataRepositoryPort.findAll()).thenReturn(mockNodes);

        // Act
        masterTreeProvider.init();

        // Assert
        verify(masterDataRepositoryPort, times(1)).findAll();
        assertNotNull(masterTreeProvider.getTree(), "El árbol debería haberse instanciado en el init");
    }
}
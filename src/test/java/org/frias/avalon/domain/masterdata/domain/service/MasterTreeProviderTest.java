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

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MasterTreeProviderTest {

    @Mock
    private MasterDataRepositoryPort masterPort;

    @InjectMocks
    private MasterTreeProvider masterTreeProvider;

    private MasterRoot masterRoot1;
    private MasterRoot masterRoot2;

    @BeforeEach
    void setUp() {
        masterRoot1 = new MasterRoot(1L, "ACT", "Activo", null, 1L);
        masterRoot2 = new MasterRoot(2L, "INA", "Inactivo", null, 1L);
    }

    @Test
    @DisplayName("should initialize MasterTree on PostConstruct")
    void init_shouldInitializeMasterTree() {
        // Given
        List<MasterRoot> mockMasterRoots = Arrays.asList(masterRoot1, masterRoot2);
        when(masterPort.findAll()).thenReturn(mockMasterRoots);

        // When
        // Simulate @PostConstruct call
        masterTreeProvider.init();

        // Then
        assertThat(masterTreeProvider.getTree()).isNotNull();
        assertThat(masterTreeProvider.getTree().getById(masterRoot1.getId())).isEqualTo(masterRoot1);
        assertThat(masterTreeProvider.getTree().getById(masterRoot2.getId())).isEqualTo(masterRoot2);
        verify(masterPort).findAll(); // Ensure findAll was called
    }

    @Test
    @DisplayName("refresh should update the MasterTree with latest data")
    void refresh_shouldUpdateMasterTree() {
        // Given
        List<MasterRoot> initialMasterRoots = Arrays.asList(masterRoot1);
        when(masterPort.findAll()).thenReturn(initialMasterRoots);
        masterTreeProvider.init(); // Initialize with some data

        MasterTree initialTree = masterTreeProvider.getTree();
        assertThat(initialTree.getById(masterRoot1.getId())).isEqualTo(masterRoot1);
        assertThat(initialTree.getById(masterRoot2.getId())).isNull(); // masterRoot2 not in initial tree

        // When
        List<MasterRoot> updatedMasterRoots = Arrays.asList(masterRoot1, masterRoot2);
        when(masterPort.findAll()).thenReturn(updatedMasterRoots); // Simulate new data from repository
        masterTreeProvider.refresh();

        // Then
        MasterTree updatedTree = masterTreeProvider.getTree();
        assertThat(updatedTree).isNotNull().isNotSameAs(initialTree); // Should be a new instance
        assertThat(updatedTree.getById(masterRoot1.getId())).isEqualTo(masterRoot1);
        assertThat(updatedTree.getById(masterRoot2.getId())).isEqualTo(masterRoot2); // masterRoot2 should now be in the tree
        verify(masterPort, org.mockito.Mockito.times(2)).findAll(); // findAll called twice (init + refresh)
    }

    @Test
    @DisplayName("getTree should return the current MasterTree instance")
    void getTree_shouldReturnCurrentMasterTreeInstance() {
        // Given
        List<MasterRoot> mockMasterRoots = Arrays.asList(masterRoot1);
        when(masterPort.findAll()).thenReturn(mockMasterRoots);
        masterTreeProvider.init();

        // When
        MasterTree tree = masterTreeProvider.getTree();

        // Then
        assertThat(tree).isNotNull();
        assertThat(tree.getById(masterRoot1.getId())).isEqualTo(masterRoot1);
    }
}

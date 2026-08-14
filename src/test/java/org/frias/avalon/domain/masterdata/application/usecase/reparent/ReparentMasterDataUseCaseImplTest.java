package org.frias.avalon.domain.masterdata.application.usecase.reparent;

import org.frias.avalon.domain.masterdata.application.dto.response.MasterDataResponseDto;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.repository.MasterDataRepositoryPort;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReparentMasterDataUseCaseImplTest {

    @Mock
    private MasterDataRepositoryPort masterDataRepositoryPort;

    @Mock
    private MasterTreeProvider masterTreeProvider;

    @InjectMocks
    private ReparentMasterDataUseCaseImpl reparentMasterDataUseCase;

    @Test
    @DisplayName("Deberia cambiar el padre de un nodo y retornar MasterDataResponseDto")
    void shouldReparentNodeSuccessfully() {
        // Arrange
        Long nodeId = 10L;
        Long newParentId = 2L;
        MasterRoot updatedNode = MasterRoot.fromPersistence(nodeId, "CHILD", "FULL CHILD", newParentId, 1L);

        given(masterDataRepositoryPort.updateParentId(nodeId, newParentId)).willReturn(updatedNode);

        // Act
        MasterDataResponseDto response = reparentMasterDataUseCase.execute(nodeId, newParentId);

        // Assert
        assertNotNull(response);
        assertEquals(nodeId, response.id());
        assertEquals("CHILD", response.shortName());
        assertEquals("FULL CHILD", response.fullName());
        verify(masterDataRepositoryPort).updateParentId(nodeId, newParentId);
        verify(masterTreeProvider).refresh();
    }
}

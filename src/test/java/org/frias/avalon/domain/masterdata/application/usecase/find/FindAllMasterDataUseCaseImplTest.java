package org.frias.avalon.domain.masterdata.application.usecase.find;

import org.frias.avalon.domain.masterdata.application.dto.response.MasterDataResponseDto;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.repository.MasterDataRepositoryPort;
import org.frias.avalon.domain.masterdata.infraestructure.mapper.MasterDataMapperService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias para FindAllMasterDataUseCase")
class FindAllMasterDataUseCaseImplTest {

    @Mock
    private MasterDataRepositoryPort masterDataRepositoryPort;

    @Mock
    private MasterDataMapperService mapper;

    @InjectMocks
    private FindAllMasterDataUseCaseImpl findAllMasterDataUseCase;

    @Test
    @DisplayName("Debería retornar una lista vacía si no hay MasterData")
    void shouldReturnEmptyListWhenNoMasterData() {
        // Arrange
        given(masterDataRepositoryPort.findAll()).willReturn(Arrays.asList());

        // Act
        List<MasterDataResponseDto> result = findAllMasterDataUseCase.execute();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(masterDataRepositoryPort).findAll();
    }

    @Test
    @DisplayName("Debería retornar todos los MasterData como DTOs")
    void shouldReturnAllMasterDataAsDtos() {
        // Arrange
        MasterRoot master1 = new MasterRoot(1L, "SHORT1", "FULL1", null, 1L);
        MasterRoot master2 = new MasterRoot(2L, "SHORT2", "FULL2", 1L, 1L);
        List<MasterRoot> mockDomainList = Arrays.asList(master1, master2);

        MasterDataResponseDto dto1 = new MasterDataResponseDto(1L, "SHORT1", "FULL1");
        MasterDataResponseDto dto2 = new MasterDataResponseDto(2L, "SHORT2", "FULL2");
        List<MasterDataResponseDto> expectedDtoList = Arrays.asList(dto1, dto2);

        given(masterDataRepositoryPort.findAll()).willReturn(mockDomainList);
        given(mapper.toResponse(master1)).willReturn(dto1);
        given(mapper.toResponse(master2)).willReturn(dto2);

        // Act
        List<MasterDataResponseDto> result = findAllMasterDataUseCase.execute();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expectedDtoList, result);

        verify(masterDataRepositoryPort).findAll();
        verify(mapper).toResponse(master1);
        verify(mapper).toResponse(master2);
    }
}

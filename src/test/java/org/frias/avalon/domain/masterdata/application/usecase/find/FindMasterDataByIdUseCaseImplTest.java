package org.frias.avalon.domain.masterdata.application.usecase.find;

import jakarta.persistence.EntityNotFoundException;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias para FindMasterDataByIdUseCase")
class FindMasterDataByIdUseCaseImplTest {

    @Mock
    private MasterDataRepositoryPort masterDataRepositoryPort;

    @Mock
    private MasterDataMapperService mapper;

    @InjectMocks
    private FindMasterDataByIdUseCaseImpl findMasterDataByIdUseCase;

    @Test
    @DisplayName("Debería encontrar un MasterData y retornarlo como DTO")
    void shouldFindMasterDataAndReturnDto() {
        // Arrange
        Long expectedId = 1L;
        MasterRoot mockDomain = new MasterRoot(expectedId, "SHORT", "FULL", 100L, 200L);
        MasterDataResponseDto expectedDto = new MasterDataResponseDto(expectedId, "SHORT", "FULL");

        given(masterDataRepositoryPort.findById(expectedId)).willReturn(Optional.of(mockDomain));
        given(mapper.toResponse(mockDomain)).willReturn(expectedDto);

        // Act
        MasterDataResponseDto result = findMasterDataByIdUseCase.execute(expectedId);

        // Assert
        assertNotNull(result);
        assertEquals(expectedId, result.id());
        assertEquals("SHORT", result.shortName());

        verify(masterDataRepositoryPort).findById(expectedId);
        verify(mapper).toResponse(mockDomain);
    }

    @Test
    @DisplayName("Debería lanzar EntityNotFoundException si el ID no existe")
    void shouldThrowExceptionWhenIdNotFound() {
        // Arrange
        Long nonExistentId = 99L;
        given(masterDataRepositoryPort.findById(nonExistentId)).willReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            findMasterDataByIdUseCase.execute(nonExistentId);
        });

        assertEquals("No existe la clave MasterData", exception.getMessage());
        verify(masterDataRepositoryPort).findById(nonExistentId);
    }
}

package org.frias.avalon.domain.masterdata.application.usecase.changestatus;

import jakarta.persistence.EntityNotFoundException;
import org.frias.avalon.core.exeptions.DomainValidationException;
import org.frias.avalon.domain.masterdata.application.dto.request.MasterDataUpdateStatusDto;
import org.frias.avalon.domain.masterdata.application.dto.response.MasterDataResponseDto;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.repository.MasterDataRepositoryPort;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.masterdata.infraestructure.mapper.MasterDataMapperService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias para ChangeStatusUseCase")
class ChangeStatusUseCaseImplTest {

    @Mock
    private MasterDataRepositoryPort masterDataRepositoryPort;
    @Mock
    private MasterDataMapperService mapper;
    @Mock
    private MasterTreeProvider masterTreeProvider;

    @InjectMocks
    private ChangeStatusUseCaseImpl changeStatusUseCase;

    private MasterDataUpdateStatusDto validRequestDto;
    private MasterRoot currentMasterRoot;
    private MasterRoot nextStatusMasterRoot;
    private MasterDataResponseDto expectedResponseDto;

    @BeforeEach
    void setUp() {
        validRequestDto = new MasterDataUpdateStatusDto(1L, 2L);

        currentMasterRoot = new MasterRoot(1L, "ACT", "Activo", null, 1L);
        nextStatusMasterRoot = new MasterRoot(2L, "INA", "Inactivo", null, 1L);

        expectedResponseDto = new MasterDataResponseDto(1L, "ACT", "Activo");
    }

    @Test
    @DisplayName("Debería cambiar el estado de MasterData exitosamente")
    void shouldChangeMasterDataStatusSuccessfully() {
        // Arrange
        given(masterDataRepositoryPort.findById(validRequestDto.current())).willReturn(Optional.of(currentMasterRoot));
        given(masterDataRepositoryPort.findById(validRequestDto.next())).willReturn(Optional.of(nextStatusMasterRoot));
        given(masterDataRepositoryPort.save(any(MasterRoot.class))).willReturn(currentMasterRoot);
        given(mapper.toResponse(any(MasterRoot.class))).willReturn(expectedResponseDto);

        // Act
        MasterDataResponseDto result = changeStatusUseCase.execute(validRequestDto);

        // Assert
        assertNotNull(result);
        assertEquals(expectedResponseDto, result);
        assertEquals(nextStatusMasterRoot.getId(), currentMasterRoot.getStatusId());

        verify(masterDataRepositoryPort).findById(validRequestDto.current());
        verify(masterDataRepositoryPort).findById(validRequestDto.next());
        verify(masterDataRepositoryPort).save(currentMasterRoot);
        verify(masterTreeProvider).refresh();
        verify(mapper).toResponse(currentMasterRoot);
    }

    @Test
    @DisplayName("Debería lanzar EntityNotFoundException si el MasterData actual no existe")
    void shouldThrowExceptionWhenCurrentMasterDataNotFound() {
        // Arrange
        given(masterDataRepositoryPort.findById(validRequestDto.current())).willReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            changeStatusUseCase.execute(validRequestDto);
        });

        assertEquals("No existe el tipo a actualizar MasterData", exception.getMessage());
        
        verify(masterDataRepositoryPort).findById(validRequestDto.current());
        verify(masterDataRepositoryPort, never()).findById(validRequestDto.next());
        verify(masterDataRepositoryPort, never()).save(any());
        verify(masterTreeProvider, never()).refresh();
    }

    @Test
    @DisplayName("Debería lanzar EntityNotFoundException si el MasterData del nuevo estado no existe")
    void shouldThrowExceptionWhenNextStatusMasterDataNotFound() {
        // Arrange
        given(masterDataRepositoryPort.findById(validRequestDto.current())).willReturn(Optional.of(currentMasterRoot));
        given(masterDataRepositoryPort.findById(validRequestDto.next())).willReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            changeStatusUseCase.execute(validRequestDto);
        });

        assertEquals("No existe la clave MasterData", exception.getMessage());
        
        verify(masterDataRepositoryPort).findById(validRequestDto.current());
        verify(masterDataRepositoryPort).findById(validRequestDto.next());
        verify(masterDataRepositoryPort, never()).save(any());
        verify(masterTreeProvider, never()).refresh();
    }

    @Test
    @DisplayName("Debería lanzar DomainValidationException si la transición de estado es inválida")
    void shouldThrowDomainValidationExceptionForInvalidTransition() {
        // Arrange
        MasterRoot invalidCurrentMasterRoot = spy(new MasterRoot(1L, "ACT", "Activo", null, 1L));
        doThrow(new DomainValidationException("Ya tiene ese status")).when(invalidCurrentMasterRoot).changeStatus(anyLong());

        given(masterDataRepositoryPort.findById(validRequestDto.current())).willReturn(Optional.of(invalidCurrentMasterRoot));
        given(masterDataRepositoryPort.findById(validRequestDto.next())).willReturn(Optional.of(nextStatusMasterRoot));

        // Act & Assert
        DomainValidationException exception = assertThrows(DomainValidationException.class, () -> {
            changeStatusUseCase.execute(validRequestDto);
        });

        assertEquals("Ya tiene ese status", exception.getMessage());
        
        verify(masterDataRepositoryPort).findById(validRequestDto.current());
        verify(masterDataRepositoryPort).findById(validRequestDto.next());
        verify(masterDataRepositoryPort, never()).save(any());
        verify(masterTreeProvider, never()).refresh();
    }
}

package org.frias.avalon.module.masterdata.services;

import jakarta.persistence.EntityNotFoundException;
import org.frias.avalon.domain.masterdata.dtos.request.MasterDataNewDto;
import org.frias.avalon.domain.masterdata.entities.MasterData;
import org.frias.avalon.domain.masterdata.repositories.MasterDataRepository;
import org.frias.avalon.domain.masterdata.services.implementation.MasterDataServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MasterDataServiceTest {

    @Mock
    private MasterDataRepository repository;

    @InjectMocks
    private MasterDataServiceImpl service;

    private MasterData statusActive;

    @BeforeEach
    void setUp() {
        statusActive = new MasterData(2L, "ACTIVO", "ACT", 1L, 1L);
    }

    @Test
    @DisplayName("Create: Debe guardar un MasterData y asignar estatus activo")
    void shouldCreateMasterData() {
        // Given
        MasterDataNewDto dto = new MasterDataNewDto("NUEVO DATO", "NEW", null, "ACT");
        
        // Mockeamos la búsqueda del status ACT que hace el servicio internamente
        given(repository.findByShortNameAndStatusActive("ACT")).willReturn(Optional.of(statusActive));
        given(repository.save(any(MasterData.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(repository.saveAndFlush(any(MasterData.class))).willAnswer(invocation -> invocation.getArgument(0));

        // When
        MasterData result = service.create(dto);

        // Then
        assertThat(result.getShortName()).isEqualTo("NEW");
        assertThat(result.getStatusId()).isEqualTo(2L); // ID de ACT
        verify(repository).saveAndFlush(any(MasterData.class));
    }

    @Test
    @DisplayName("SearchByShortName: Debe retornar el dato si existe y está activo")
    void shouldSearchByShortName() {
        given(repository.findByShortNameAndStatusActive("TEST")).willReturn(Optional.of(new MasterData(10L, "TEST", "TEST", null, 2L)));

        MasterData result = service.searchByShortName("TEST");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("SearchByShortName: Debe lanzar excepción si no existe")
    void shouldThrowExceptionWhenNotFound() {
        given(repository.findByShortNameAndStatusActive("UNKNOWN")).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.searchByShortName("UNKNOWN"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("GetRootBranch: Debe escalar la jerarquía hasta encontrar la raíz especificada")
    void shouldFindRootBranch() {
        // Jerarquía: ROOT -> LEVEL1 -> LEVEL2 (Target)
        MasterData root = new MasterData(100L, "ROOT", "ROOT", null, 2L);
        MasterData level1 = new MasterData(101L, "LEVEL1", "L1", 100L, 2L);
        MasterData level2 = new MasterData(102L, "LEVEL2", "L2", 101L, 2L);

        // Mockeamos las llamadas recursivas a searchById
        // NOTA: Tu servicio usa findByIdAndStatusActive dentro de searchById, y findById para padres en getRootBranch
        // Ajustamos los mocks según tu implementación actual:
        
        // Mock para searchById(102) -> level2
        given(repository.findByIdAndStatusActive(102L)).willReturn(Optional.of(level2));
        
        // Mock para parents (findById simple)
        given(repository.findById(101L)).willReturn(Optional.of(level1)); // Padre de L2
        given(repository.findById(100L)).willReturn(Optional.of(root));   // Padre de L1

        // Ejecutamos
        MasterData result = service.getRootBranch(102L, "ROOT");

        assertThat(result).isNotNull();
        assertThat(result.getShortName()).isEqualTo("LEVEL2"); // Tu lógica devuelve 'current' si el padre es ROOT
    }
}

package org.frias.avalon.domain.masterdata.application.usecase.create;

import org.frias.avalon.domain.masterdata.application.dto.request.MasterDataNewDto;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.repository.MasterDataRepositoryPort;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.jdbc.Sql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias para CreateMasterDataUseCase")

class CreateMasterDataUseCaseImplTest {

    @Mock
    private MasterDataRepositoryPort masterDataRepositoryPort;

    @Mock
    private MasterTreeProvider masterTreeProvider;

    @InjectMocks
    private CreateMasterDataUseCaseImpl createMasterDataUseCase;

    private MasterDataNewDto validRequestDto;

    @BeforeEach
    void setUp() {
        validRequestDto = new MasterDataNewDto(
                "Full Name Test",
                "SHORT_TEST",
                "PARENT_TEST",
                "ACT"
        );
    }

    @Test
    @DisplayName("Debería orquestar la creación de un MasterData exitosamente")
    void shouldOrchestrateMasterDataCreationSuccessfully() {
        // Arrange (Given)
        Long parentId = 100L;
        Long statusId = 200L;
        Long expectedId = 1L;

        // Configuración de Mocks
        given(masterDataRepositoryPort.getIdByCode("PARENT_TEST")).willReturn(parentId);
        given(masterDataRepositoryPort.getIdByCode("ACT")).willReturn(statusId);

        // Usamos un ArgumentCaptor para inspeccionar el objeto que se pasa al método save
        ArgumentCaptor<MasterRoot> masterRootCaptor = ArgumentCaptor.forClass(MasterRoot.class);
        
        // Simulamos el objeto que se retorna después de guardar
        MasterRoot savedMasterRoot = new MasterRoot(expectedId, "SHORT_TEST", "FULL NAME TEST", parentId, statusId);
        given(masterDataRepositoryPort.save(masterRootCaptor.capture())).willReturn(savedMasterRoot);

        // Act (When)
        Long resultId = createMasterDataUseCase.execute(validRequestDto);

        // Assert (Then)
        assertEquals(expectedId, resultId);

        // Verificamos las interacciones con los mocks
        verify(masterDataRepositoryPort).getIdByCode("PARENT_TEST");
        verify(masterDataRepositoryPort).getIdByCode("ACT");
        verify(masterDataRepositoryPort).save(any(MasterRoot.class));
        verify(masterTreeProvider).refresh();

        // Verificamos el contenido del objeto pasado a save()
        MasterRoot capturedMasterRoot = masterRootCaptor.getValue();
        assertNull(capturedMasterRoot.getId(), "El ID debe ser nulo antes de guardar");
        assertEquals("SHORT_TEST", capturedMasterRoot.getShortName());
        assertEquals("FULL NAME TEST", capturedMasterRoot.getFullName());
        assertEquals(parentId, capturedMasterRoot.getParentId());
        assertEquals(statusId, capturedMasterRoot.getStatusId());
    }

    @ParameterizedTest(name = "fullName=''{0}'', shortName=''{1}''")
    @CsvSource({
            ", SHORT_TEST, fullName requerido",
            "'', SHORT_TEST, fullName requerido",
            "'   ', SHORT_TEST, fullName requerido",
            "Full Name, , shortName requerido",
            "Full Name, '', shortName requerido",
            "Full Name, '   ', shortName requerido"
    })
    @DisplayName("Debería lanzar excepción y no guardar si los datos son inválidos")
    void shouldThrowExceptionAndNotSaveForInvalidInputs(String fullName, String shortName, String expectedMessage) {
        // Arrange (Given)
        MasterDataNewDto invalidDto = new MasterDataNewDto(fullName, shortName, "STSGEN", "ACT");

        System.out.println("fullName: " + invalidDto.fullName());
        System.out.println("shortName: " + invalidDto.shortName());
        System.out.println("parentCode: " + invalidDto.parentShortName());
        System.out.println("statusCode: " + invalidDto.statusId());

        // Simulamos las llamadas que ocurren ANTES de la validación del dominio
        //given(masterDataRepositoryPort.getIdByCode("STSGEN")).willReturn(100L);
        //given(masterDataRepositoryPort.getIdByCode("ACT")).willReturn(200L);

        // Act & Assert (When & Then)
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            createMasterDataUseCase.execute(invalidDto);
        });

        assertEquals(expectedMessage, exception.getMessage());

        // Verificación clave: nos aseguramos de que NUNCA se intentó guardar ni refrescar
        verify(masterDataRepositoryPort, never()).save(any(MasterRoot.class));
        verify(masterTreeProvider, never()).refresh();
    }
}

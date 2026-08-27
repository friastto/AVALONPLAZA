package org.frias.avalon.domain.outlet.application.usecase.create;

import org.frias.avalon.core.exeptions.DomainValidationException;
import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.core.tenant.port.TenantSchemaMigrationPort;
import org.frias.avalon.domain.masterdata.application.dto.response.StatusResponseDto;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.repository.MasterDataRepositoryPort;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.outlet.application.dto.LocationDto;
import org.frias.avalon.domain.outlet.application.dto.request.OutletCreateRequestDto;
import org.frias.avalon.domain.outlet.application.dto.response.OutletResponseDto;
import org.frias.avalon.domain.outlet.domain.model.LocationDomain;
import org.frias.avalon.domain.outlet.domain.model.OutletDomain;
import org.frias.avalon.domain.outlet.domain.port.OutletRepositoryPort;
import org.frias.avalon.domain.outlet.infraestructure.mapper.LocationMapper;
import org.frias.avalon.domain.outlet.infraestructure.mapper.OutletMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias para CreateOutletUseCaseImpl")
class CreateOutletUseCaseImplTest {

    @Mock
    private OutletRepositoryPort outletPort;

    @Mock
    private MasterDataRepositoryPort masterPort;

    @Mock
    private MasterTreeProvider masterTreeProvider;

    @Mock
    private OutletMapper outletMapper;

    @Mock
    private LocationMapper locationMapper;

    @Mock
    private TenantSchemaMigrationPort tenantSchemaMigrationPort;

    @InjectMocks
    private CreateOutletUseCaseImpl createOutletUseCase;

    @Test
    @DisplayName("Deberia crear una tienda exitosamente cuando companyId esta presente")
    void shouldCreateOutletSuccessfullyWhenCompanyIdIsProvided() {
        // Arrange
        LocationDto locationDto = new LocationDto(4.60971, -74.08175);
        OutletCreateRequestDto requestDto = new OutletCreateRequestDto(
                "Tienda Central",
                "Calle 100 #15-30",
                "3001234567",
                "900123456-1",
                locationDto,
                50L
        );

        MasterRoot activeStatus = MasterRoot.fromPersistence(1L, "ACT", "Activo", null, 1L);
        given(masterPort.getActiveStatus()).willReturn(Optional.of(activeStatus));
        given(masterTreeProvider.getTree()).willReturn(mock(MasterTree.class));

        LocationDomain locationDomain = new LocationDomain(4.60971, -74.08175);
        OutletDomain savedOutlet = OutletDomain.fromPersistence(
                100L,
                "OUT-100",
                "Tienda Central",
                "Calle 100 #15-30",
                "3001234567",
                "900123456-1",
                1L,
                locationDomain,
                BigDecimal.ZERO,
                false,
                BigDecimal.ZERO,
                50L,
                LocalDateTime.now(),
                null
        );

        given(outletPort.save(any(OutletDomain.class))).willReturn(savedOutlet);
        given(locationMapper.domainToDto(savedOutlet.getLocation())).willReturn(locationDto);

        // Act
        OutletResponseDto response = createOutletUseCase.execute(requestDto);

        // Assert
        assertNotNull(response);
        assertEquals(100L, response.id());
        assertEquals("OUT-100", response.code());
        assertEquals("Tienda Central", response.name());
        assertEquals("Calle 100 #15-30", response.address());
        assertEquals("3001234567", response.phone());
        assertEquals("900123456-1", response.nit());
        assertEquals(50L, response.companyId());
        assertEquals(locationDto, response.location());

        verify(masterPort).getActiveStatus();
        verify(masterTreeProvider).getTree();
        verify(outletPort).save(any(OutletDomain.class));
        verify(tenantSchemaMigrationPort).migrateTenantSchema("company_50");
    }

    @Test
    @DisplayName("Deberia crear una tienda exitosamente cuando companyId es nulo")
    void shouldCreateOutletSuccessfullyWhenCompanyIdIsNull() {
        // Arrange
        LocationDto locationDto = new LocationDto(6.25184, -75.56359);
        OutletCreateRequestDto requestDto = new OutletCreateRequestDto(
                "Tienda Independiente",
                "Carrera 43A #30-40",
                "3119876543",
                "900987654-2",
                locationDto,
                null
        );

        MasterRoot activeStatus = MasterRoot.fromPersistence(1L, "ACT", "Activo", null, 1L);
        given(masterPort.getActiveStatus()).willReturn(Optional.of(activeStatus));
        given(masterTreeProvider.getTree()).willReturn(mock(MasterTree.class));

        LocationDomain locationDomain = new LocationDomain(6.25184, -75.56359);
        OutletDomain savedOutlet = OutletDomain.fromPersistence(
                200L,
                "OUT-200",
                "Tienda Independiente",
                "Carrera 43A #30-40",
                "3119876543",
                "900987654-2",
                1L,
                locationDomain,
                BigDecimal.ZERO,
                false,
                BigDecimal.ZERO,
                null,
                LocalDateTime.now(),
                null
        );

        given(outletPort.save(any(OutletDomain.class))).willReturn(savedOutlet);
        given(locationMapper.domainToDto(savedOutlet.getLocation())).willReturn(locationDto);

        // Act
        OutletResponseDto response = createOutletUseCase.execute(requestDto);

        // Assert
        assertNotNull(response);
        assertEquals(200L, response.id());
        assertNull(response.companyId());

        verify(tenantSchemaMigrationPort).migrateTenantSchema("store_200");
    }

    @Test
    @DisplayName("Deberia lanzar ResourceNotFoundException cuando no se encuentra el estado activo")
    void shouldThrowResourceNotFoundExceptionWhenActiveStatusNotFound() {
        // Arrange
        LocationDto locationDto = new LocationDto(4.60971, -74.08175);
        OutletCreateRequestDto requestDto = new OutletCreateRequestDto(
                "Tienda Sin Estado",
                "Calle 1",
                "12345",
                "900000000",
                locationDto,
                10L
        );

        given(masterPort.getActiveStatus()).willReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> createOutletUseCase.execute(requestDto)
        );

        assertEquals("No se pudo activar la tienda en este momento", exception.getMessage());
        verify(masterPort).getActiveStatus();
        verifyNoInteractions(masterTreeProvider);
        verifyNoInteractions(outletPort);
        verifyNoInteractions(tenantSchemaMigrationPort);
    }

    @Test
    @DisplayName("Deberia lanzar DomainValidationException cuando los datos de la tienda son invalidos")
    void shouldThrowDomainValidationExceptionWhenOutletDataIsInvalid() {
        // Arrange
        LocationDto locationDto = new LocationDto(4.60971, -74.08175);
        OutletCreateRequestDto requestDto = new OutletCreateRequestDto(
                "", // nombre en blanco
                "Calle 1",
                "12345",
                "900000000",
                locationDto,
                10L
        );

        MasterRoot activeStatus = MasterRoot.fromPersistence(1L, "ACT", "Activo", null, 1L);
        given(masterPort.getActiveStatus()).willReturn(Optional.of(activeStatus));
        given(masterTreeProvider.getTree()).willReturn(mock(MasterTree.class));

        // Act & Assert
        DomainValidationException exception = assertThrows(
                DomainValidationException.class,
                () -> createOutletUseCase.execute(requestDto)
        );

        assertEquals("El nombre de la tienda no puede estar vacio", exception.getMessage());
        verifyNoInteractions(outletPort);
        verifyNoInteractions(tenantSchemaMigrationPort);
    }
}

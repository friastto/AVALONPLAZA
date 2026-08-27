package org.frias.avalon.domain.outlet.infraestructure.persistence.adapter;

import org.frias.avalon.domain.outlet.application.dto.request.OutletSearchCriteria;
import org.frias.avalon.domain.outlet.domain.model.LocationDomain;
import org.frias.avalon.domain.outlet.domain.model.OutletDomain;
import org.frias.avalon.domain.outlet.domain.model.OutletLocationInfo;
import org.frias.avalon.domain.outlet.infraestructure.entities.Outlet;
import org.frias.avalon.domain.outlet.infraestructure.mapper.LocationMapper;
import org.frias.avalon.domain.outlet.infraestructure.mapper.OutletMapper;
import org.frias.avalon.domain.outlet.infraestructure.repository.JpaOutletRepository;
import org.frias.avalon.domain.outlet.infraestructure.repository.OutletLightProjection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias para OutletRepositoryAdapter")
class OutletRepositoryAdapterTest {

    @Mock
    private JpaOutletRepository jpa;

    @Mock
    private OutletMapper outletMapper;

    @Mock
    private LocationMapper locationMapper;

    @InjectMocks
    private OutletRepositoryAdapter adapter;

    @Test
    @DisplayName("Deberia guardar un OutletDomain correctamente")
    void shouldSaveOutletDomainSuccessfully() {
        // Arrange
        LocationDomain locationDomain = new LocationDomain(4.60971, -74.08175);
        OutletDomain domainToSave = OutletDomain.create(
                "Tienda Central", "Calle 100", "3001234567", "900123456-1", 1L, locationDomain
        );

        Outlet entityToSave = new Outlet();
        Outlet savedEntity = new Outlet();
        savedEntity.setId(1L);

        OutletDomain savedDomain = OutletDomain.fromPersistence(
                1L, "OUT-001", "Tienda Central", "Calle 100", "3001234567", "900123456-1", 1L, locationDomain
        );

        given(outletMapper.toEntity(domainToSave)).willReturn(entityToSave);
        given(jpa.save(entityToSave)).willReturn(savedEntity);
        given(outletMapper.toDomain(savedEntity)).willReturn(savedDomain);

        // Act
        OutletDomain result = adapter.save(domainToSave);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Tienda Central", result.getName());

        verify(outletMapper).toEntity(domainToSave);
        verify(jpa).save(entityToSave);
        verify(outletMapper).toDomain(savedEntity);
    }

    @Test
    @DisplayName("Deberia retornar un OutletDomain por ID si existe")
    void shouldFindByIdWhenOutletExists() {
        // Arrange
        Long id = 1L;
        Outlet entity = new Outlet();
        LocationDomain locationDomain = new LocationDomain(4.60971, -74.08175);
        OutletDomain domain = OutletDomain.fromPersistence(
                id, "OUT-001", "Tienda Central", "Calle 100", "3001234567", "900123456-1", 1L, locationDomain
        );

        given(jpa.findById(id)).willReturn(Optional.of(entity));
        given(outletMapper.toDomain(entity)).willReturn(domain);

        // Act
        Optional<OutletDomain> result = adapter.findById(id);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
        verify(jpa).findById(id);
        verify(outletMapper).toDomain(entity);
    }

    @Test
    @DisplayName("Deberia retornar Optional.empty() al buscar por ID si no existe")
    void shouldReturnEmptyOptionalWhenFindByIdNotFound() {
        // Arrange
        Long id = 99L;
        given(jpa.findById(id)).willReturn(Optional.empty());

        // Act
        Optional<OutletDomain> result = adapter.findById(id);

        // Assert
        assertTrue(result.isEmpty());
        verify(jpa).findById(id);
        verifyNoInteractions(outletMapper);
    }

    @Test
    @DisplayName("Deberia buscar tienda por NIT exitosamente")
    void shouldFindByNitWhenOutletExists() {
        // Arrange
        String nit = "900123456-1";
        Outlet entity = new Outlet();
        LocationDomain locationDomain = new LocationDomain(4.60971, -74.08175);
        OutletDomain domain = OutletDomain.fromPersistence(
                1L, "OUT-001", "Tienda Central", "Calle 100", "3001234567", nit, 1L, locationDomain
        );

        given(jpa.findByNit(nit)).willReturn(Optional.of(entity));
        given(outletMapper.toDomain(entity)).willReturn(domain);

        // Act
        Optional<OutletDomain> result = adapter.findByNit(nit);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(nit, result.get().getNit());
        verify(jpa).findByNit(nit);
    }

    @Test
    @DisplayName("Deberia buscar tiendas por companyId exitosamente")
    void shouldFindByCompanyIdReturnsListOfOutlets() {
        // Arrange
        Long companyId = 10L;
        Outlet entity1 = new Outlet();
        Outlet entity2 = new Outlet();

        LocationDomain loc1 = new LocationDomain(4.60971, -74.08175);
        LocationDomain loc2 = new LocationDomain(6.25184, -75.56359);

        OutletDomain domain1 = OutletDomain.fromPersistence(
                1L, "OUT-001", "Tienda 1", "Calle 100", "3001234567", "900123456-1", 1L, loc1, BigDecimal.ZERO, false, BigDecimal.ZERO, companyId, LocalDateTime.now(), null
        );
        OutletDomain domain2 = OutletDomain.fromPersistence(
                2L, "OUT-002", "Tienda 2", "Carrera 43A", "3119876543", "900987654-2", 1L, loc2, BigDecimal.ZERO, false, BigDecimal.ZERO, companyId, LocalDateTime.now(), null
        );

        given(jpa.findByCompanyId(companyId)).willReturn(List.of(entity1, entity2));
        given(outletMapper.toDomain(entity1)).willReturn(domain1);
        given(outletMapper.toDomain(entity2)).willReturn(domain2);

        // Act
        List<OutletDomain> result = adapter.findByCompanyId(companyId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(domain1, result.get(0));
        assertEquals(domain2, result.get(1));
        verify(jpa).findByCompanyId(companyId);
    }

    @Test
    @DisplayName("Deberia buscar todas las tiendas con criterios de busqueda y paginacion")
    void shouldFindAllWithSearchCriteriaAndPageable() {
        // Arrange
        OutletSearchCriteria criteria = new OutletSearchCriteria("Tienda", "900", "OUT-1", "Calle", 1L);
        Pageable pageable = PageRequest.of(0, 10);
        Outlet entity = new Outlet();
        LocationDomain loc = new LocationDomain(4.60971, -74.08175);
        OutletDomain domain = OutletDomain.fromPersistence(
                1L, "OUT-001", "Tienda Central", "Calle 100", "3001234567", "900123456-1", 1L, loc
        );

        given(jpa.findAll(any(Specification.class), eq(pageable))).willReturn(new PageImpl<>(List.of(entity)));
        given(outletMapper.toDomain(entity)).willReturn(domain);

        // Act
        Page<OutletDomain> result = adapter.findAll(criteria, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(domain, result.getContent().get(0));
        verify(jpa).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    @DisplayName("Deberia buscar tiendas cercanas por radio")
    void shouldFindNearbyByRadius() {
        // Arrange
        LocationDomain location = new LocationDomain(4.60971, -74.08175);
        int radius = 5000;
        Outlet entity = new Outlet();
        OutletDomain domain = OutletDomain.fromPersistence(
                1L, "OUT-001", "Tienda Cercana", "Calle 100", "3001234567", "900123456-1", 1L, location
        );

        given(jpa.findNearByOrderByDistance(-74.08175, 4.60971, radius)).willReturn(List.of(entity));
        given(outletMapper.toDomain(entity)).willReturn(domain);

        // Act
        List<OutletDomain> result = adapter.findNearbyByRadius(location, radius);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(domain, result.get(0));
        verify(jpa).findNearByOrderByDistance(-74.08175, 4.60971, radius);
    }

    @Test
    @DisplayName("Deberia buscar informacion ligera de tiendas cercanas por radio")
    void shouldFindNearbyByRadiusLight() {
        // Arrange
        Double lat = 4.60971;
        Double lon = -74.08175;
        int radius = 3000;

        OutletLightProjection projection = mock(OutletLightProjection.class);
        when(projection.getId()).thenReturn(1L);
        when(projection.getName()).thenReturn("Tienda Light");
        when(projection.getLatitude()).thenReturn(lat);
        when(projection.getLongitude()).thenReturn(lon);

        given(jpa.findNearbyByRadiusLight(lat, lon, radius)).willReturn(List.of(projection));

        // Act
        List<OutletLocationInfo> result = adapter.findNearbyByRadiusLight(lat, lon, radius);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        OutletLocationInfo info = result.get(0);
        assertEquals(1L, info.id());
        assertEquals("Tienda Light", info.name());
        assertEquals(lat, info.latitude());
        assertEquals(lon, info.longitude());
        verify(jpa).findNearbyByRadiusLight(lat, lon, radius);
    }

    @Test
    @DisplayName("Deberia verificar comportamiento de metodos no implementados o con valores por defecto")
    void shouldVerifyUnimplementedOrDefaultMethods() {
        // Act & Assert
        assertTrue(adapter.nearbyByName("test").isEmpty());
        assertNull(adapter.update(null));
        assertNull(adapter.delete(null));
    }
}

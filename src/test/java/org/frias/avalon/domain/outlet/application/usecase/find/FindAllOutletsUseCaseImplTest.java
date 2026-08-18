package org.frias.avalon.domain.outlet.application.usecase.find;

import org.frias.avalon.domain.outlet.application.dto.LocationDto;
import org.frias.avalon.domain.outlet.application.dto.request.OutletSearchCriteria;
import org.frias.avalon.domain.outlet.application.dto.response.OutletResponseDto;
import org.frias.avalon.domain.outlet.domain.model.LocationDomain;
import org.frias.avalon.domain.outlet.domain.model.OutletDomain;
import org.frias.avalon.domain.outlet.domain.port.OutletRepositoryPort;
import org.frias.avalon.domain.outlet.infraestructure.mapper.OutletMapper;
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

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias para FindAllOutletsUseCase con Paginación y Filtrado Dinámico")
class FindAllOutletsUseCaseImplTest {

    @Mock
    private OutletRepositoryPort outletPort;

    @Mock
    private OutletMapper outletMapper;

    @InjectMocks
    private FindAllOutletsUseCaseImpl findAllOutletsUseCase;

    @Test
    @DisplayName("Debería retornar una página vacía si no hay tiendas que coincidan con el filtro")
    void shouldReturnEmptyPageWhenNoOutletsMatchFilter() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        OutletSearchCriteria criteria = new OutletSearchCriteria("Tienda Inexistente", null, null, null, null);
        given(outletPort.findAll(criteria, pageable)).willReturn(new PageImpl<>(Collections.emptyList()));

        // Act
        Page<OutletResponseDto> result = findAllOutletsUseCase.execute(criteria, pageable);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(outletPort).findAll(criteria, pageable);
    }

    @Test
    @DisplayName("Debería retornar una página con todas las tiendas registradas mapeadas a DTO")
    void shouldReturnPaginatedRegisteredOutletsAsDtos() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        OutletSearchCriteria criteria = new OutletSearchCriteria(null, null, null, null, null);
        LocationDomain locationDomain1 = new LocationDomain(4.60971, -74.08175);
        LocationDomain locationDomain2 = new LocationDomain(6.25184, -75.56359);

        OutletDomain outlet1 = OutletDomain.fromPersistence(
                1L, "OUTLET001", "Tienda Norte", "Calle 100 #15-30", "1234567", "900.123.456-1", 1L, locationDomain1
        );
        OutletDomain outlet2 = OutletDomain.fromPersistence(
                2L, "OUTLET002", "Tienda Sur", "Carrera 43A #30-40", "7654321", "900.765.432-2", 1L, locationDomain2
        );
        List<OutletDomain> mockOutletsList = List.of(outlet1, outlet2);
        Page<OutletDomain> mockOutletsPage = new PageImpl<>(mockOutletsList, pageable, mockOutletsList.size());

        LocationDto locationDto1 = new LocationDto(4.60971, -74.08175);
        LocationDto locationDto2 = new LocationDto(6.25184, -75.56359);

        OutletResponseDto dto1 = new OutletResponseDto(
                1L, "OUTLET001", "Tienda Norte", "Calle 100 #15-30", "1234567", "900.123.456-1", locationDto1, null, null, false, java.math.BigDecimal.ZERO
        );
        OutletResponseDto dto2 = new OutletResponseDto(
                2L, "OUTLET002", "Tienda Sur", "Carrera 43A #30-40", "7654321", "900.765.432-2", locationDto2, null, null, false, java.math.BigDecimal.ZERO
        );

        given(outletPort.findAll(criteria, pageable)).willReturn(mockOutletsPage);
        given(outletMapper.toResponse(outlet1)).willReturn(dto1);
        given(outletMapper.toResponse(outlet2)).willReturn(dto2);

        // Act
        Page<OutletResponseDto> result = findAllOutletsUseCase.execute(criteria, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(dto1, result.getContent().get(0));
        assertEquals(dto2, result.getContent().get(1));

        verify(outletPort).findAll(criteria, pageable);
        verify(outletMapper).toResponse(outlet1);
        verify(outletMapper).toResponse(outlet2);
    }
}

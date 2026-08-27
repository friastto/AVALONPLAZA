package org.frias.avalon.domain.product.infraestructure.mapper;

import org.frias.avalon.core.permissions.CurrentUserProviderPort;
import org.frias.avalon.domain.masterdata.application.dto.response.MasterDataResponseDto;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.repository.MasterDataRepositoryPort;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.masterdata.infraestructure.mapper.MasterDataMapperService;
import org.frias.avalon.domain.order.infrastructure.persistence.repository.JpaOrderRepository;
import org.frias.avalon.domain.product.application.dto.response.ProductResponse;
import org.frias.avalon.domain.product.domain.ProductDomain;
import org.frias.avalon.domain.product.domain.service.UnitConversionService;
import org.frias.avalon.domain.product.infraestructure.entity.ProductOutlet;
import org.frias.avalon.domain.user.domain.port.UserAvalonRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Unit Tests for ProductOutletMapper Infrastructure Layer")
class ProductOutletMapperTest {

    private MasterTreeProvider masterTreeProvider;
    private MasterDataMapperService masterDataMapper;
    private UnitConversionService unitConversionService;
    private JpaOrderRepository jpaOrderRepository;
    private CurrentUserProviderPort currentUserProvider;
    private UserAvalonRepositoryPort userAvalonRepositoryPort;
    private MasterDataRepositoryPort masterDataRepositoryPort;

    private ProductOutletMapperImpl productOutletMapper;

    @BeforeEach
    void setUp() {
        masterTreeProvider = mock(MasterTreeProvider.class);
        masterDataMapper = mock(MasterDataMapperService.class);
        unitConversionService = mock(UnitConversionService.class);
        jpaOrderRepository = mock(JpaOrderRepository.class);
        currentUserProvider = mock(CurrentUserProviderPort.class);
        userAvalonRepositoryPort = mock(UserAvalonRepositoryPort.class);
        masterDataRepositoryPort = mock(MasterDataRepositoryPort.class);

        productOutletMapper = new ProductOutletMapperImpl(
                masterTreeProvider,
                masterDataMapper,
                unitConversionService,
                jpaOrderRepository,
                currentUserProvider,
                userAvalonRepositoryPort,
                masterDataRepositoryPort
        );
    }

    @Test
    @DisplayName("Should map ProductOutlet entity to ProductDomain and back to entity")
    void shouldMapEntityToDomainAndEntity() {
        ProductOutlet entity = ProductOutlet.builder()
                .id(10L)
                .localName("Aceite Vegetal")
                .localDescription("1L Aceite")
                .stock(15)
                .unitMeasureId(1L)
                .localPrice(new BigDecimal("9500.00"))
                .outletId(2L)
                .statusId(1L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        ProductDomain domain = productOutletMapper.toDomain(entity);
        assertNotNull(domain);
        assertEquals(10L, domain.getId());
        assertEquals("Aceite Vegetal", domain.getName());

        ProductOutlet mappedBack = productOutletMapper.toEntity(domain);
        assertNotNull(mappedBack);
        assertEquals(10L, mappedBack.getId());
        assertEquals("Aceite Vegetal", mappedBack.getLocalName());
    }

    @Test
    @DisplayName("Should map ProductDomain to ProductResponse DTO with status and stock formatting")
    void shouldMapDomainToProductResponse() {
        ProductDomain domain = ProductDomain.fromPersistence(
                10L, "Aceite Vegetal", "1L Aceite", 15, 1L, "https://cdn.com/oil.jpg",
                new BigDecimal("9500.00"), 2L, 1L, LocalDateTime.now(), LocalDateTime.now()
        );

        MasterTree masterTree = mock(MasterTree.class);
        MasterRoot statusNode = new MasterRoot(1L, "ACT", "Activo", 0L, 1L);

        when(masterTreeProvider.getTree()).thenReturn(masterTree);
        when(masterTree.getById(1L)).thenReturn(statusNode);

        MasterDataResponseDto statusDto = new MasterDataResponseDto(1L, "ACT", "Activo", null, null);
        when(masterDataMapper.toResponse(statusNode)).thenReturn(statusDto);

        when(unitConversionService.convertFromSmallestUnit(15, 1L)).thenReturn("15 UN");
        when(unitConversionService.convertFromSmallestUnit(0, 1L)).thenReturn("0 UN");

        ProductResponse response = productOutletMapper.toResponse(domain, "7701234567890");

        assertNotNull(response);
        assertEquals(10L, response.id());
        assertEquals("Aceite Vegetal", response.name());
        assertEquals("15 UN", response.displayStock());
        assertEquals("7701234567890", response.barCode());
        assertEquals("https://cdn.com/oil.jpg", response.effectiveImageUrl());
    }
}

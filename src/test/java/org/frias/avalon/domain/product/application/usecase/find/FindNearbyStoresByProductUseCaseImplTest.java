package org.frias.avalon.domain.product.application.usecase.find;

import org.frias.avalon.core.tenant.TenantContext;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.outlet.application.dto.LocationDto;
import org.frias.avalon.domain.outlet.domain.model.OutletLocationInfo;
import org.frias.avalon.domain.outlet.domain.port.OutletRepositoryPort;
import org.frias.avalon.domain.product.application.dto.request.NearbyStoresByProductRequestDto;
import org.frias.avalon.domain.product.application.dto.response.NearbyStoreProductResponseDto;
import org.frias.avalon.domain.product.application.port.ProductOutletRepositoryPort;
import org.frias.avalon.domain.product.domain.ProductDomain;
import org.frias.avalon.domain.product.domain.service.UnitConversionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("Unit Tests for FindNearbyStoresByProductUseCaseImpl")
class FindNearbyStoresByProductUseCaseImplTest {

    private OutletRepositoryPort outletRepositoryPort;
    private ProductOutletRepositoryPort productOutletRepositoryPort;
    private UnitConversionService unitConversionService;
    private MasterTreeProvider masterTreeProvider;
    private MasterTree masterTree;
    private PlatformTransactionManager transactionManager;

    private FindNearbyStoresByProductUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        outletRepositoryPort = mock(OutletRepositoryPort.class);
        productOutletRepositoryPort = mock(ProductOutletRepositoryPort.class);
        unitConversionService = mock(UnitConversionService.class);
        masterTreeProvider = mock(MasterTreeProvider.class);
        masterTree = mock(MasterTree.class);
        when(masterTreeProvider.getTree()).thenReturn(masterTree);
        transactionManager = mock(PlatformTransactionManager.class);

        TransactionStatus transactionStatus = mock(TransactionStatus.class);
        when(transactionManager.getTransaction(any())).thenReturn(transactionStatus);

        useCase = new FindNearbyStoresByProductUseCaseImpl(
                outletRepositoryPort,
                productOutletRepositoryPort,
                unitConversionService,
                masterTreeProvider,
                transactionManager
        );
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Should return empty list when request or query is empty")
    void shouldReturnEmptyWhenRequestOrQueryIsEmpty() {
        List<NearbyStoreProductResponseDto> r1 = useCase.execute(null);
        List<NearbyStoreProductResponseDto> r2 = useCase.execute(new NearbyStoresByProductRequestDto(null, 2000, "limon"));
        List<NearbyStoreProductResponseDto> r3 = useCase.execute(new NearbyStoresByProductRequestDto(
                new LocationDto(4.65, -74.05), 2000, ""));

        assertTrue(r1.isEmpty());
        assertTrue(r2.isEmpty());
        assertTrue(r3.isEmpty());
        verifyNoInteractions(outletRepositoryPort);
    }

    @Test
    @DisplayName("Should return stores that have the matching products in stock with unit conversion")
    void shouldReturnStoresWithMatchingProducts() {
        OutletLocationInfo outlet1 = new OutletLocationInfo(1L, "Tienda Norte", 4.65, -74.05);
        OutletLocationInfo outlet2 = new OutletLocationInfo(2L, "Tienda Sur", 4.66, -74.06);

        when(outletRepositoryPort.findNearbyByRadiusLight(4.65, -74.05, 2000, null))
                .thenReturn(List.of(outlet1, outlet2));

        LocalDateTime now = LocalDateTime.now();
        ProductDomain p1 = ProductDomain.fromPersistence(101L, "Limon Mandarina", "Desc", 1000, 1L, null, BigDecimal.TEN, 1L, 1L, now, now);
        Page<ProductDomain> page1 = new PageImpl<>(List.of(p1));
        Page<ProductDomain> emptyPage = new PageImpl<>(List.of());

        when(productOutletRepositoryPort.findAvailableByName(eq("limon"), eq(1L), any(Pageable.class)))
                .thenReturn(page1);
        when(productOutletRepositoryPort.findAvailableByName(eq("limon"), eq(2L), any(Pageable.class)))
                .thenReturn(emptyPage);

        when(unitConversionService.convertFromSmallestUnit(1000, 1L)).thenReturn("1.000 KG");
        MasterRoot unitNode = MasterRoot.fromPersistence(1L, "KG", "Kilogramo", 100L, 1L);
        when(masterTree.getById(1L)).thenReturn(unitNode);

        NearbyStoresByProductRequestDto request = new NearbyStoresByProductRequestDto(
                new LocationDto(4.65, -74.05),
                2000,
                "limon"
        );

        List<NearbyStoreProductResponseDto> result = useCase.execute(request);

        assertNotNull(result);
        assertEquals(1, result.size());
        NearbyStoreProductResponseDto store = result.get(0);
        assertEquals(1L, store.id());
        assertEquals("Tienda Norte", store.name());
        assertEquals(1, store.matchingProducts().size());
        assertEquals("Limon Mandarina", store.matchingProducts().get(0).productName());
        assertEquals(1000, store.matchingProducts().get(0).stock());
        assertEquals("1.000 KG", store.matchingProducts().get(0).displayStock());
        assertEquals("KG", store.matchingProducts().get(0).unitMeasure());
    }
}

package org.frias.avalon.domain.product.application.usecase.find;

import org.frias.avalon.core.tenant.TenantContext;
import org.frias.avalon.domain.outlet.domain.model.OutletLocationInfo;
import org.frias.avalon.domain.outlet.domain.port.OutletRepositoryPort;
import org.frias.avalon.domain.product.application.port.ProductOutletRepositoryPort;
import org.frias.avalon.domain.product.domain.ProductDomain;
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

@DisplayName("Unit Tests for FindNearbyProductSuggestionsUseCaseImpl")
class FindNearbyProductSuggestionsUseCaseImplTest {

    private OutletRepositoryPort outletRepositoryPort;
    private ProductOutletRepositoryPort productOutletRepositoryPort;
    private PlatformTransactionManager transactionManager;

    private FindNearbyProductSuggestionsUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        outletRepositoryPort = mock(OutletRepositoryPort.class);
        productOutletRepositoryPort = mock(ProductOutletRepositoryPort.class);
        transactionManager = mock(PlatformTransactionManager.class);

        TransactionStatus transactionStatus = mock(TransactionStatus.class);
        when(transactionManager.getTransaction(any())).thenReturn(transactionStatus);

        useCase = new FindNearbyProductSuggestionsUseCaseImpl(
                outletRepositoryPort,
                productOutletRepositoryPort,
                transactionManager
        );
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Should return empty list when query is null or shorter than 2 characters")
    void shouldReturnEmptyWhenQueryIsShort() {
        List<String> nullResult = useCase.execute(4.65, -74.05, 2000, null);
        List<String> emptyResult = useCase.execute(4.65, -74.05, 2000, " ");
        List<String> oneCharResult = useCase.execute(4.65, -74.05, 2000, "a");

        assertTrue(nullResult.isEmpty());
        assertTrue(emptyResult.isEmpty());
        assertTrue(oneCharResult.isEmpty());
        verifyNoInteractions(outletRepositoryPort);
    }

    @Test
    @DisplayName("Should return empty list when coordinates are null")
    void shouldReturnEmptyWhenCoordinatesNull() {
        List<String> result = useCase.execute(null, -74.05, 2000, "limon");
        assertTrue(result.isEmpty());
        verifyNoInteractions(outletRepositoryPort);
    }

    @Test
    @DisplayName("Should return empty list when no outlets nearby")
    void shouldReturnEmptyWhenNoOutletsNearby() {
        when(outletRepositoryPort.findNearbyByRadiusLight(4.65, -74.05, 2000, null))
                .thenReturn(List.of());

        List<String> result = useCase.execute(4.65, -74.05, 2000, "limon");

        assertTrue(result.isEmpty());
        verify(outletRepositoryPort).findNearbyByRadiusLight(4.65, -74.05, 2000, null);
    }

    @Test
    @DisplayName("Should find distinct product suggestions across nearby outlets")
    void shouldFindDistinctSuggestionsAcrossOutlets() {
        OutletLocationInfo outlet1 = new OutletLocationInfo(1L, "Tienda Norte", 4.65, -74.05);
        OutletLocationInfo outlet2 = new OutletLocationInfo(2L, "Tienda Sur", 4.66, -74.06);

        when(outletRepositoryPort.findNearbyByRadiusLight(4.65, -74.05, 2000, null))
                .thenReturn(List.of(outlet1, outlet2));

        LocalDateTime now = LocalDateTime.now();
        ProductDomain p1 = ProductDomain.fromPersistence(101L, "Limon Mandarina", "Desc", 10, 1L, null, BigDecimal.TEN, 1L, 1L, now, now);
        ProductDomain p2 = ProductDomain.fromPersistence(102L, "Jugo de Limon", "Desc", 5, 1L, null, BigDecimal.TEN, 1L, 1L, now, now);
        ProductDomain p3Duplicate = ProductDomain.fromPersistence(103L, "Limon Mandarina", "Desc", 8, 1L, null, BigDecimal.TEN, 2L, 1L, now, now);
        ProductDomain p4 = ProductDomain.fromPersistence(104L, "Limonada Natural", "Desc", 12, 1L, null, BigDecimal.TEN, 2L, 1L, now, now);

        Page<ProductDomain> page1 = new PageImpl<>(List.of(p1, p2));
        Page<ProductDomain> page2 = new PageImpl<>(List.of(p3Duplicate, p4));

        when(productOutletRepositoryPort.findAvailableByName(eq("lim"), eq(1L), any(Pageable.class)))
                .thenReturn(page1);
        when(productOutletRepositoryPort.findAvailableByName(eq("lim"), eq(2L), any(Pageable.class)))
                .thenReturn(page2);

        List<String> suggestions = useCase.execute(4.65, -74.05, 2000, "lim");

        assertNotNull(suggestions);
        assertEquals(3, suggestions.size());
        assertEquals("Limon Mandarina", suggestions.get(0));
        assertEquals("Jugo de Limon", suggestions.get(1));
        assertEquals("Limonada Natural", suggestions.get(2));
    }
}

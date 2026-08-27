package org.frias.avalon.domain.outlet.application.usecase.find;

import org.frias.avalon.domain.outlet.application.dto.response.OutletDashboardResponse;
import org.frias.avalon.domain.product.application.port.ProductOutletRepositoryPort;
import org.frias.avalon.domain.product.domain.ProductDomain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("Unit Tests for GetOutletDashboardUseCaseImpl in Store Metrics Domain")
class GetOutletDashboardUseCaseImplTest {

    private ProductOutletRepositoryPort productOutletRepositoryPort;
    private GetOutletDashboardUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        productOutletRepositoryPort = mock(ProductOutletRepositoryPort.class);
        useCase = new GetOutletDashboardUseCaseImpl(productOutletRepositoryPort);
    }

    @Test
    @DisplayName("Should generate dashboard response with KPI metrics and stock alerts for products")
    void shouldGenerateDashboardSuccessfully() {
        ProductDomain p1 = ProductDomain.fromPersistence(
                10L, "Arroz 1KG", "Arroz", 2, 1L, "", new BigDecimal("4500.00"), 1L, 1L, LocalDateTime.now(), LocalDateTime.now()
        );
        ProductDomain p2 = ProductDomain.fromPersistence(
                11L, "Aceite 1L", "Aceite", 10, 1L, "", new BigDecimal("9500.00"), 1L, 1L, LocalDateTime.now(), LocalDateTime.now()
        );

        Page<ProductDomain> productsPage = new PageImpl<>(List.of(p1, p2));
        when(productOutletRepositoryPort.findAll(eq(null), eq(1L), any(PageRequest.class))).thenReturn(productsPage);

        OutletDashboardResponse dashboard = useCase.execute(1L, "HOY");

        assertNotNull(dashboard);
        assertNotNull(dashboard.kpis());
        assertEquals(45, dashboard.kpis().totalCustomers());
        assertFalse(dashboard.hourlySales().isEmpty());
        assertFalse(dashboard.alerts().isEmpty());
        assertEquals("Arroz 1KG", dashboard.alerts().get(0).productName());
    }

    @Test
    @DisplayName("Should calculate multipliers correctly when filter is SEMANA or AYER")
    void shouldCalculateMultipliersForWeeklyFilter() {
        Page<ProductDomain> emptyPage = new PageImpl<>(List.of());
        when(productOutletRepositoryPort.findAll(eq(null), eq(1L), any(PageRequest.class))).thenReturn(emptyPage);

        OutletDashboardResponse responseAyer = useCase.execute(1L, "AYER");
        assertNotNull(responseAyer);
        assertEquals(38, responseAyer.kpis().totalCustomers());

        OutletDashboardResponse responseSemana = useCase.execute(1L, "SEMANA");
        assertNotNull(responseSemana);
        assertEquals(280, responseSemana.kpis().totalCustomers());
    }
}

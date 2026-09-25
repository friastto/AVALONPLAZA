package org.frias.avalon.domain.outlet.application.usecase.find;

import org.frias.avalon.domain.cashregister.application.port.CashSessionRepositoryPort;
import org.frias.avalon.domain.cashregister.domain.CashSessionDomain;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.outlet.application.dto.response.HourlySalesDto;
import org.frias.avalon.domain.outlet.application.dto.response.OutletDashboardResponse;
import org.frias.avalon.domain.outlet.domain.model.OutletDomain;
import org.frias.avalon.domain.outlet.domain.port.OutletRepositoryPort;
import org.frias.avalon.domain.person.domain.model.PersonDomain;
import org.frias.avalon.domain.person.domain.port.PersonRepositoryPort;
import org.frias.avalon.domain.product.application.port.ProductOutletRepositoryPort;
import org.frias.avalon.domain.product.domain.ProductDomain;
import org.frias.avalon.domain.sale.application.port.SaleRepositoryPort;
import org.frias.avalon.domain.sale.domain.SaleDomain;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.frias.avalon.domain.user.domain.port.UserAvalonRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests for GetOutletDashboardUseCaseImpl with Real Multi-Tenant Data")
class GetOutletDashboardUseCaseImplTest {

    @Mock
    private ProductOutletRepositoryPort productOutletRepositoryPort;

    @Mock
    private SaleRepositoryPort saleRepositoryPort;

    @Mock
    private OutletRepositoryPort outletRepositoryPort;

    @Mock
    private CashSessionRepositoryPort cashSessionRepositoryPort;

    @Mock
    private PersonRepositoryPort personRepositoryPort;

    @Mock
    private UserAvalonRepositoryPort userAvalonRepositoryPort;

    @Mock
    private MasterTreeProvider masterTreeProvider;

    @Mock
    private TransactionTemplate transactionTemplate;

    @InjectMocks
    private GetOutletDashboardUseCaseImpl useCase;

    private MasterTree masterTree;

    @BeforeEach
    void setUp() {
        MasterRoot efeNode = new MasterRoot(139L, "EFE", "EFECTIVO", null, 1L);
        MasterRoot trfNode = new MasterRoot(297L, "TRF", "TRANSFERENCIA", null, 1L);
        masterTree = new MasterTree(List.of(efeNode, trfNode));
        lenient().when(masterTreeProvider.getTree()).thenReturn(masterTree);

        lenient().when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(null);
        });
    }

    @Test
    @DisplayName("Should generate dashboard with real sales KPIs, hourly chart and active registers")
    void shouldGenerateRealDashboardSuccessfully() {
        Long outletId = 1L;
        Long companyId = 10L;

        OutletDomain outlet = mock(OutletDomain.class);
        when(outlet.getCompanyId()).thenReturn(companyId);

        LocalDateTime todayTenAm = LocalDate.now().atTime(10, 30);
        LocalDateTime todayTwoPm = LocalDate.now().atTime(14, 15);

        SaleDomain saleCash = SaleDomain.fromPersistence(
                1L, UUID.randomUUID(), new BigDecimal("50000.00"), new BigDecimal("50000.00"), BigDecimal.ZERO,
                139L, 1L, 1L, outletId, 1L, todayTenAm, todayTenAm, todayTenAm, Collections.emptyList()
        );

        SaleDomain saleTransfer = SaleDomain.fromPersistence(
                2L, UUID.randomUUID(), new BigDecimal("25000.00"), new BigDecimal("25000.00"), BigDecimal.ZERO,
                297L, 1L, 2L, outletId, 1L, todayTwoPm, todayTwoPm, todayTwoPm, Collections.emptyList()
        );

        ProductDomain p1 = ProductDomain.fromPersistence(
                10L, "Arroz 1KG", "Arroz", 2, 1L, "", new BigDecimal("4500.00"), 1L, 1L, LocalDateTime.now(), LocalDateTime.now()
        );
        ProductDomain p2 = ProductDomain.fromPersistence(
                11L, "Aceite 1L", "Aceite", 10, 1L, "", new BigDecimal("9500.00"), 1L, 1L, LocalDateTime.now(), LocalDateTime.now()
        );
        Page<ProductDomain> productsPage = new PageImpl<>(List.of(p1, p2));

        CashSessionDomain activeSession = CashSessionDomain.open(outletId, 5L, new BigDecimal("100000.00"));

        UserAvalonDomain user = mock(UserAvalonDomain.class);
        when(user.getPersonId()).thenReturn(10L);

        PersonDomain person = mock(PersonDomain.class);
        when(person.getName()).thenReturn("Viviana");
        when(person.getLastName()).thenReturn("Alvarez");

        when(outletRepositoryPort.findById(outletId)).thenReturn(Optional.of(outlet));
        when(saleRepositoryPort.findByOutletAndDateBetween(eq(outletId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(saleCash, saleTransfer));
        when(productOutletRepositoryPort.findAll(eq(null), eq(outletId), any(PageRequest.class)))
                .thenReturn(productsPage);
        when(cashSessionRepositoryPort.findAllSessionsByOutlet(outletId))
                .thenReturn(List.of(activeSession));
        when(userAvalonRepositoryPort.findById(5L)).thenReturn(Optional.of(user));
        when(personRepositoryPort.findById(10L)).thenReturn(Optional.of(person));

        // Act
        OutletDashboardResponse dashboard = useCase.execute(outletId, "HOY");

        // Assert
        assertNotNull(dashboard);
        assertEquals(new BigDecimal("50000.00"), dashboard.kpis().totalCash());
        assertEquals(new BigDecimal("25000.00"), dashboard.kpis().totalTransfer());
        assertEquals(2, dashboard.kpis().totalCustomers());
        assertEquals(new BigDecimal("37500.00"), dashboard.kpis().averageTicket());

        // Hourly sales assertion
        List<HourlySalesDto> hourly = dashboard.hourlySales();
        assertEquals(7, hourly.size());
        HourlySalesDto tenAmDto = hourly.stream().filter(h -> "10:00".equals(h.hour())).findFirst().orElseThrow();
        assertEquals(new BigDecimal("50000"), tenAmDto.amount());
        HourlySalesDto twoPmDto = hourly.stream().filter(h -> "14:00".equals(h.hour())).findFirst().orElseThrow();
        assertEquals(new BigDecimal("25000"), twoPmDto.amount());

        // Alerts assertion: only real low stock product, zero fake products
        assertEquals(1, dashboard.alerts().size());
        assertEquals("Arroz 1KG", dashboard.alerts().get(0).productName());
        assertFalse(dashboard.alerts().stream().anyMatch(a -> a.productName().contains("Demo")));

        // Active registers assertion
        assertEquals(1, dashboard.activeRegisters().size());
        assertEquals("Viviana Alvarez", dashboard.activeRegisters().get(0).employeeName());
        assertTrue(dashboard.activeRegisters().get(0).isOpen());
    }

    @Test
    @DisplayName("Should return clean zero metrics when no sales and no alerts exist")
    void shouldReturnZeroKpisWhenNoSalesRecorded() {
        Long outletId = 1L;
        OutletDomain outlet = mock(OutletDomain.class);
        when(outlet.getCompanyId()).thenReturn(2L);

        Page<ProductDomain> emptyProducts = new PageImpl<>(Collections.emptyList());

        when(outletRepositoryPort.findById(outletId)).thenReturn(Optional.of(outlet));
        when(saleRepositoryPort.findByOutletAndDateBetween(eq(outletId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        when(productOutletRepositoryPort.findAll(eq(null), eq(outletId), any(PageRequest.class)))
                .thenReturn(emptyProducts);
        when(cashSessionRepositoryPort.findAllSessionsByOutlet(outletId))
                .thenReturn(Collections.emptyList());

        // Act
        OutletDashboardResponse dashboard = useCase.execute(outletId, "SEMANA");

        // Assert
        assertNotNull(dashboard);
        assertEquals(BigDecimal.ZERO, dashboard.kpis().totalCash());
        assertEquals(BigDecimal.ZERO, dashboard.kpis().totalTransfer());
        assertEquals(0, dashboard.kpis().totalCustomers());
        assertEquals(BigDecimal.ZERO, dashboard.kpis().averageTicket());
        assertTrue(dashboard.alerts().isEmpty());
        assertTrue(dashboard.activeRegisters().isEmpty());
    }
}

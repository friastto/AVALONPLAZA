package org.frias.avalon.domain.company.application.usecase.find;

import org.frias.avalon.domain.company.application.dto.response.CompanyDashboardResponse;
import org.frias.avalon.domain.company.infrastructure.entity.CompanyEntity;
import org.frias.avalon.domain.company.infrastructure.repository.JpaCompanyRepository;
import org.frias.avalon.domain.outlet.infraestructure.entities.Outlet;
import org.frias.avalon.domain.outlet.infraestructure.repository.JpaOutletRepository;
import org.frias.avalon.domain.sale.infrastructure.entity.SaleEntity;
import org.frias.avalon.domain.sale.infrastructure.repository.JpaSaleRepository;
import org.frias.avalon.domain.cashregister.infrastructure.entity.CashSessionEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests for GetCompanyDashboardUseCaseImpl")
class GetCompanyDashboardUseCaseImplTest {

    @Mock
    private JpaCompanyRepository companyRepository;

    @Mock
    private JpaOutletRepository outletRepository;

    @Mock
    private JpaSaleRepository saleRepository;

    @Mock
    private org.frias.avalon.domain.cashregister.infrastructure.repository.JpaCashSessionRepository cashSessionRepository;

    @Mock
    private TransactionTemplate transactionTemplate;

    @Mock
    private org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider masterTreeProvider;

    @InjectMocks
    private GetCompanyDashboardUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        org.frias.avalon.domain.masterdata.domain.model.MasterRoot efeNode = new org.frias.avalon.domain.masterdata.domain.model.MasterRoot(139L, "EFE", "EFECTIVO", null, 1L);
        org.frias.avalon.domain.masterdata.domain.model.MasterRoot fiaNode = new org.frias.avalon.domain.masterdata.domain.model.MasterRoot(151L, "FIA", "FIADO", null, 1L);
        org.frias.avalon.domain.masterdata.domain.model.MasterRoot trfNode = new org.frias.avalon.domain.masterdata.domain.model.MasterRoot(297L, "TRF", "TRANSFERENCIA", null, 1L);
        org.frias.avalon.domain.masterdata.domain.model.MasterTree masterTree = new org.frias.avalon.domain.masterdata.domain.model.MasterTree(List.of(efeNode, fiaNode, trfNode));
        lenient().when(masterTreeProvider.getTree()).thenReturn(masterTree);

        lenient().when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(null);
        });
    }

    @Test
    @DisplayName("Should consolidate company dashboard metrics for month period across multiple outlets")
    void shouldConsolidateCompanyDashboardSuccessfully() {
        // Arrange
        Long companyId = 1L;
        CompanyEntity company = new CompanyEntity();
        company.setId(companyId);
        company.setName("Empresa Matriz Avalon");

        Outlet outlet1 = new Outlet();
        outlet1.setId(10L);
        outlet1.setName("Sede Centro");

        Outlet outlet2 = new Outlet();
        outlet2.setId(20L);
        outlet2.setName("Sede Norte");

        SaleEntity sale1 = SaleEntity.builder()
                .id(101L)
                .saleCode(UUID.randomUUID())
                .outletId(10L)
                .totalAmount(new BigDecimal("100000.00"))
                .paymentMethodId(139L)
                .saleDate(LocalDateTime.now())
                .build();

        SaleEntity sale2 = SaleEntity.builder()
                .id(102L)
                .saleCode(UUID.randomUUID())
                .outletId(20L)
                .totalAmount(new BigDecimal("200000.00"))
                .paymentMethodId(151L)
                .saleDate(LocalDateTime.now())
                .build();

        CashSessionEntity closedSession = new CashSessionEntity();
        closedSession.setId(501L);
        closedSession.setOutletId(10L);
        closedSession.setStatus("CLOSED");
        closedSession.setClosedAt(LocalDateTime.now());
        closedSession.setActualCash(new BigDecimal("100000.00"));

        CashSessionEntity openSession = new CashSessionEntity();
        openSession.setId(502L);
        openSession.setOutletId(20L);
        openSession.setStatus("OPEN");
        openSession.setInitialBase(new BigDecimal("20000.00"));

        given(companyRepository.findById(companyId)).willReturn(Optional.of(company));
        given(outletRepository.findByCompanyId(companyId)).willReturn(List.of(outlet1, outlet2));
        given(saleRepository.findByOutletIdInAndSaleDateBetween(eq(List.of(10L)), any(LocalDateTime.class), any(LocalDateTime.class)))
                .willReturn(List.of(sale1));
        given(saleRepository.findByOutletIdInAndSaleDateBetween(eq(List.of(20L)), any(LocalDateTime.class), any(LocalDateTime.class)))
                .willReturn(List.of(sale2));
        given(cashSessionRepository.findByOutletIdOrderByOpenedAtDesc(10L)).willReturn(List.of(closedSession));
        given(cashSessionRepository.findByOutletIdOrderByOpenedAtDesc(20L)).willReturn(List.of(openSession));

        // Act
        CompanyDashboardResponse result = useCase.execute(companyId, "MES", null);

        // Assert
        assertNotNull(result);
        assertEquals(companyId, result.companyId());
        assertEquals("Empresa Matriz Avalon", result.companyName());
        assertEquals("MES", result.period());
        assertNull(result.selectedOutletId());
        assertEquals(new BigDecimal("300000.00"), result.totalSales());
        assertEquals(BigDecimal.ZERO, result.totalExpenses());
        assertEquals(new BigDecimal("300000.00"), result.netProfit());
        assertEquals(100.0, result.profitMarginPercentage());
        assertEquals(2L, result.transactionCount());
        assertEquals(new BigDecimal("150000.00"), result.averageTicket());
        assertEquals(2, result.outletSales().size());
        assertEquals("EFECTIVO", result.salesByPaymentMethod().keySet().iterator().next());
        assertEquals(new BigDecimal("100000.00"), result.consolidatedCash());
        assertEquals(1, result.closedSessionsCount());
        assertEquals(1, result.openSessionsCount());
    }

    @Test
    @DisplayName("Should filter company dashboard by specific outlet ID")
    void shouldFilterDashboardBySpecificOutletId() {
        // Arrange
        Long companyId = 1L;
        Long outletId = 10L;

        CompanyEntity company = new CompanyEntity();
        company.setId(companyId);
        company.setName("Empresa Matriz Avalon");

        Outlet outlet1 = new Outlet();
        outlet1.setId(10L);
        outlet1.setName("Sede Centro");

        SaleEntity sale1 = SaleEntity.builder()
                .id(101L)
                .saleCode(UUID.randomUUID())
                .outletId(10L)
                .totalAmount(new BigDecimal("150000.00"))
                .paymentMethodId(139L)
                .saleDate(LocalDateTime.now())
                .build();

        given(companyRepository.findById(companyId)).willReturn(Optional.of(company));
        given(outletRepository.findByCompanyId(companyId)).willReturn(List.of(outlet1));
        given(saleRepository.findByOutletIdInAndSaleDateBetween(eq(List.of(10L)), any(LocalDateTime.class), any(LocalDateTime.class)))
                .willReturn(List.of(sale1));
        given(cashSessionRepository.findByOutletIdOrderByOpenedAtDesc(10L)).willReturn(List.of());

        // Act
        CompanyDashboardResponse result = useCase.execute(companyId, "HOY", outletId);

        // Assert
        assertNotNull(result);
        assertEquals(outletId, result.selectedOutletId());
        assertEquals("HOY", result.period());
        assertEquals(new BigDecimal("150000.00"), result.totalSales());
        assertEquals(1L, result.transactionCount());
        assertEquals(BigDecimal.ZERO, result.consolidatedCash());
        assertEquals(0, result.closedSessionsCount());
        assertEquals(0, result.openSessionsCount());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when company is not found")
    void shouldThrowExceptionWhenCompanyNotFound() {
        // Arrange
        given(companyRepository.findById(99L)).willReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(99L, "MES", null));
    }
}

package org.frias.avalon.domain.company.application.usecase.find;

import org.frias.avalon.domain.company.application.dto.response.CompanyDashboardResponse;
import org.frias.avalon.domain.company.infrastructure.entity.CompanyEntity;
import org.frias.avalon.domain.company.infrastructure.repository.JpaCompanyRepository;
import org.frias.avalon.domain.outlet.infraestructure.entities.Outlet;
import org.frias.avalon.domain.outlet.infraestructure.repository.JpaOutletRepository;
import org.frias.avalon.domain.sale.infrastructure.entity.SaleEntity;
import org.frias.avalon.domain.sale.infrastructure.repository.JpaSaleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @InjectMocks
    private GetCompanyDashboardUseCaseImpl useCase;

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

        given(companyRepository.findById(companyId)).willReturn(Optional.of(company));
        given(outletRepository.findByCompanyId(companyId)).willReturn(List.of(outlet1, outlet2));
        given(saleRepository.findByOutletIdInAndSaleDateBetween(anyList(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .willReturn(List.of(sale1, sale2));

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

        // Act
        CompanyDashboardResponse result = useCase.execute(companyId, "HOY", outletId);

        // Assert
        assertNotNull(result);
        assertEquals(outletId, result.selectedOutletId());
        assertEquals("HOY", result.period());
        assertEquals(new BigDecimal("150000.00"), result.totalSales());
        assertEquals(1L, result.transactionCount());
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

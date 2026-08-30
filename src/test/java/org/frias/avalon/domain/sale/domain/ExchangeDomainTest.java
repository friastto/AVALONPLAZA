package org.frias.avalon.domain.sale.domain;

import org.frias.avalon.core.exeptions.BusinessException;
import org.frias.avalon.core.exeptions.DomainValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit Tests for Exchange Domain Operations (Sale & Return Domain Integrations)")
class ExchangeDomainTest {

    @Nested
    @DisplayName("Exchange Resolution Type Validation")
    class ExchangeResolutionTests {

        @Test
        @DisplayName("Should create ReturnDomain with resolutionType 'CAMBIO' successfully")
        void testCreateExchangeReturnDomain() {
            ReturnItemDomain returnItem = new ReturnItemDomain(
                    1L, 10L, 2, "2 UND", new BigDecimal("5000.00"), new BigDecimal("10000.00"), 1L
            );

            ReturnDomain returnDomain = ReturnDomain.create(
                    100L,
                    "DEFECTO",
                    "Cambio por defecto de fábrica",
                    "CAMBIO",
                    50L,
                    5L,
                    1L,
                    20L,
                    List.of(returnItem)
            );

            assertNotNull(returnDomain);
            assertEquals("CAMBIO", returnDomain.getResolutionType());
            assertEquals("DEFECTO", returnDomain.getReason());
            assertEquals(new BigDecimal("10000.00"), returnDomain.getTotalRefundAmount());
        }

        @Test
        @DisplayName("Should accept case-insensitive 'cambio' resolution type")
        void testCaseInsensitiveCambioResolution() {
            ReturnItemDomain returnItem = new ReturnItemDomain(
                    1L, 10L, 1, "1 UND", new BigDecimal("3000.00"), new BigDecimal("3000.00"), 1L
            );

            ReturnDomain returnDomain = ReturnDomain.create(
                    100L, "OTRO", "Nota", "cambio", 50L, 5L, 1L, 20L, List.of(returnItem)
            );

            assertEquals("CAMBIO", returnDomain.getResolutionType());
        }
    }

    @Nested
    @DisplayName("Exchange Financial Difference & Total Calculations")
    class ExchangeCalculationsTests {

        @Test
        @DisplayName("Should calculate net surplus (Excedente) when replacement items exceed returned items value")
        void testCalculateNetDifferenceSurplus() {
            // Returned product: 1 item @ 5,000 = 5,000 total
            ReturnItemDomain returnItem = new ReturnItemDomain(1L, 10L, 1, "1 UND", new BigDecimal("5000.00"), new BigDecimal("5000.00"), 1L);
            ReturnDomain returnDomain = ReturnDomain.create(100L, "DEFECTO", "Notas", "CAMBIO", 50L, 5L, 1L, 20L, List.of(returnItem));

            // Replacement products: 2 items @ 7,000 = 14,000 total
            SaleItemDomain saleItem = new SaleItemDomain(1L, 12L, 2, "2 UND", new BigDecimal("7000.00"), new BigDecimal("14000.00"), 1L);
            SaleDomain newSale = SaleDomain.create(1L, 1L, 20L, 1L, 5L, List.of(saleItem));

            BigDecimal totalReturned = returnDomain.getTotalRefundAmount();
            BigDecimal totalNew = newSale.getTotalAmount();
            BigDecimal netDifference = totalNew.subtract(totalReturned);

            assertEquals(new BigDecimal("5000.00"), totalReturned);
            assertEquals(new BigDecimal("14000.00"), totalNew);
            assertEquals(new BigDecimal("9000.00"), netDifference);
            assertTrue(netDifference.compareTo(BigDecimal.ZERO) > 0, "Excedente must be positive when replacement is more expensive");

            // Apply cash payment for the surplus (received 10,000, difference 9,000 -> 1,000 change)
            newSale.applyPayment(new BigDecimal("14000.00"));
            assertEquals(new BigDecimal("14000.00"), newSale.getAmountReceived());
        }

        @Test
        @DisplayName("Should calculate net credit (Sobrante) when returned items exceed replacement items value")
        void testCalculateNetDifferenceSobrante() {
            // Returned product: 1 item @ 15,000 = 15,000 total
            ReturnItemDomain returnItem = new ReturnItemDomain(1L, 10L, 1, "1 UND", new BigDecimal("15000.00"), new BigDecimal("15000.00"), 1L);
            ReturnDomain returnDomain = ReturnDomain.create(100L, "DEFECTO", "Notas", "CAMBIO", 50L, 5L, 1L, 20L, List.of(returnItem));

            // Replacement products: 1 item @ 8,000 = 8,000 total
            SaleItemDomain saleItem = new SaleItemDomain(1L, 12L, 1, "1 UND", new BigDecimal("8000.00"), new BigDecimal("8000.00"), 1L);
            SaleDomain newSale = SaleDomain.create(1L, 1L, 20L, 1L, 5L, List.of(saleItem));

            BigDecimal totalReturned = returnDomain.getTotalRefundAmount();
            BigDecimal totalNew = newSale.getTotalAmount();
            BigDecimal netDifference = totalNew.subtract(totalReturned);

            assertEquals(new BigDecimal("15000.00"), totalReturned);
            assertEquals(new BigDecimal("8000.00"), totalNew);
            assertEquals(new BigDecimal("-7000.00"), netDifference);
            assertTrue(netDifference.compareTo(BigDecimal.ZERO) < 0, "Net difference must be negative when replacement is cheaper");

            // Client balance surplus to return or apply to debt
            BigDecimal surplusToClient = netDifference.abs();
            assertEquals(new BigDecimal("7000.00"), surplusToClient);
        }

        @Test
        @DisplayName("Should handle exact value exchange with net difference of zero")
        void testCalculateNetDifferenceExactValue() {
            ReturnItemDomain returnItem = new ReturnItemDomain(1L, 10L, 2, "2 UND", new BigDecimal("5000.00"), new BigDecimal("10000.00"), 1L);
            ReturnDomain returnDomain = ReturnDomain.create(100L, "INCORRECTO", "Notas", "CAMBIO", 50L, 5L, 1L, 20L, List.of(returnItem));

            SaleItemDomain saleItem = new SaleItemDomain(1L, 15L, 1, "1 UND", new BigDecimal("10000.00"), new BigDecimal("10000.00"), 1L);
            SaleDomain newSale = SaleDomain.create(1L, 1L, 20L, 1L, 5L, List.of(saleItem));

            BigDecimal netDifference = newSale.getTotalAmount().subtract(returnDomain.getTotalRefundAmount());
            assertEquals(0, netDifference.compareTo(BigDecimal.ZERO));

            newSale.applyPayment(new BigDecimal("10000.00"), false);
            assertEquals(new BigDecimal("10000.00"), newSale.getAmountReceived());
            assertEquals(0, BigDecimal.ZERO.compareTo(newSale.getChangeGiven()));
        }

        @Test
        @DisplayName("Should apply fiado payment mode for surplus exchange without requiring cash amount")
        void testExchangeFiadoSurplusPayment() {
            SaleItemDomain saleItem = new SaleItemDomain(1L, 12L, 1, "1 UND", new BigDecimal("20000.00"), new BigDecimal("20000.00"), 1L);
            SaleDomain newSale = SaleDomain.create(2L, 1L, 20L, 1L, 5L, List.of(saleItem)); // PaymentMethod 2 = Fiado

            newSale.applyPayment(BigDecimal.ZERO, true);

            assertEquals(0, BigDecimal.ZERO.compareTo(newSale.getAmountReceived()));
            assertEquals(0, BigDecimal.ZERO.compareTo(newSale.getChangeGiven()));
        }
    }
}

package org.frias.avalon.domain.sale.domain;

import org.frias.avalon.core.exeptions.BusinessException;
import org.frias.avalon.core.exeptions.DomainValidationException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SaleDomainTest {

    @Test
    void testCreateSaleSuccessfully() {
        SaleItemDomain item = new SaleItemDomain(1L, 10L, 5, "5 UND", new BigDecimal("100"), new BigDecimal("500"), 2L);
        SaleDomain sale = SaleDomain.create(
                1L, // paymentMethodId
                2L, // statusId
                3L, // clientId
                4L, // outletId
                5L, // employeeId
                List.of(item)
        );

        assertNotNull(sale.getSaleCode());
        assertEquals(new BigDecimal("500"), sale.getTotalAmount());
        assertEquals(1L, sale.getPaymentMethodId());
        assertEquals(2L, sale.getStatusId());
        assertEquals(3L, sale.getClientId());
        assertEquals(4L, sale.getOutletId());
        assertEquals(5L, sale.getEmployeeId());
        assertEquals(1, sale.getItems().size());
    }

    @Test
    void testCreateSaleWithoutItemsThrowsException() {
        assertThrows(DomainValidationException.class, () -> SaleDomain.create(
                1L, 2L, 3L, 4L, 5L, List.of()
        ));
    }

    @Test
    void testApplyPaymentSuccessfullyAndCalculatesChangeCorrectly() {
        SaleItemDomain item = new SaleItemDomain(1L, 10L, 1, "1 UND", new BigDecimal("150"), new BigDecimal("150"), 2L);
        SaleDomain sale = SaleDomain.create(1L, 2L, 3L, 4L, 5L, List.of(item));

        // Total = 150. Paga con 200.
        sale.applyPayment(new BigDecimal("200"));

        assertEquals(new BigDecimal("200"), sale.getAmountReceived());
        assertEquals(new BigDecimal("50"), sale.getChangeGiven()); // Bug de PlazaFEC corregido (no da negativo)
    }

    @Test
    void testApplyPaymentInsufficientThrowsException() {
        SaleItemDomain item = new SaleItemDomain(1L, 10L, 1, "1 UND", new BigDecimal("150"), new BigDecimal("150"), 2L);
        SaleDomain sale = SaleDomain.create(1L, 2L, 3L, 4L, 5L, List.of(item));

        // Total = 150. Paga con 100.
        assertThrows(BusinessException.class, () -> sale.applyPayment(new BigDecimal("100")));
    }
}

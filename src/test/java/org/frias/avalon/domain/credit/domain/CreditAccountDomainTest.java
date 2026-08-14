package org.frias.avalon.domain.credit.domain;

import org.frias.avalon.domain.credit.domain.model.CreditAccountDomain;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit Tests for CreditAccountDomain Aggregate Root")
class CreditAccountDomainTest {

    @Test
    @DisplayName("Should create credit account and verify balance operations")
    void shouldCreateCreditAccountAndVerifyOperations() {
        CreditAccountDomain account = CreditAccountDomain.create(
                500L,
                10L,
                new BigDecimal("1000.00"),
                1L
        );

        assertNotNull(account);
        assertEquals(500L, account.getClientId());
        assertEquals(10L, account.getOutletId());
        assertEquals(new BigDecimal("1000.00"), account.getCreditLimit());
        assertEquals(BigDecimal.ZERO, account.getCurrentDebt());

        account.charge(new BigDecimal("250.00"));
        assertEquals(new BigDecimal("250.00"), account.getCurrentDebt());

        account.pay(new BigDecimal("100.00"));
        assertEquals(new BigDecimal("150.00"), account.getCurrentDebt());
    }
}

package org.frias.avalon.domain.cashregister.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit Tests for CashSessionDomain Aggregate Root")
class CashSessionDomainTest {

    @Test
    @DisplayName("Should create open cash session domain model correctly")
    void shouldCreateOpenCashSessionDomain() {
        CashSessionDomain session = CashSessionDomain.open(1L, 100L, new BigDecimal("100.00"));

        assertNotNull(session);
        assertEquals(1L, session.getOutletId());
        assertEquals(100L, session.getEmployeeId());
        assertEquals(new BigDecimal("100.00"), session.getInitialBase());
        assertEquals("OPEN", session.getStatus());
        assertNotNull(session.getOpenedAt());
        assertNull(session.getClosedAt());
    }

    @Test
    @DisplayName("Should perform blind count and close session")
    void shouldPerformBlindCountAndClose() {
        CashSessionDomain session = CashSessionDomain.fromPersistence(
                10L, 1L, 100L, LocalDateTime.now(), null,
                new BigDecimal("100.00"), new BigDecimal("150.00"), null, null,
                "OPEN", null, LocalDateTime.now(), LocalDateTime.now()
        );

        session.blindCount(new BigDecimal("160.00"), new BigDecimal("50.00"), BigDecimal.ZERO, BigDecimal.ZERO, "Notes");
        assertEquals("BLIND_COUNTED", session.getStatus());
        assertEquals(new BigDecimal("160.00"), session.getActualCash());

        session.closeSession();
        assertEquals("CLOSED", session.getStatus());
        assertNotNull(session.getClosedAt());
    }
}

package org.frias.avalon.domain.subscription.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit Tests for BillingCycleCalculatorService")
class BillingCycleCalculatorServiceTest {

    private BillingCycleCalculatorService calculatorService;

    @BeforeEach
    void setUp() {
        calculatorService = new BillingCycleCalculatorService();
    }

    @Test
    @DisplayName("Should calculate initial schedule with 30-day trial and day 28 alignment")
    void shouldCalculateInitialScheduleCorrectly() {
        // Given an outlet approved on March 10, 2026 at 14:00
        LocalDateTime approvedAt = LocalDateTime.of(2026, 3, 10, 14, 0, 0);

        // When
        BillingCycleCalculatorService.BillingSchedule schedule =
                calculatorService.calculateInitialSchedule(approvedAt, 28);

        // Then: Trial ends in 30 days -> April 9, 2026
        assertEquals(LocalDateTime.of(2026, 4, 9, 14, 0, 0), schedule.trialEndsAt());
        assertEquals(approvedAt, schedule.currentPeriodStart());

        // Aligned to next 28th -> April 28, 2026 at 23:59:59
        assertEquals(LocalDateTime.of(2026, 4, 28, 23, 59, 59), schedule.currentPeriodEnd());

        // Grace period ends 7 days post cut -> May 5, 2026 at 23:59:59
        assertEquals(LocalDateTime.of(2026, 5, 5, 23, 59, 59), schedule.gracePeriodEnd());
    }

    @Test
    @DisplayName("Should align to day 28 in next month if reference date is after day 28")
    void shouldAlignToNextMonthIfAfterDay28() {
        // Given reference date on April 29, 2026
        LocalDateTime refDate = LocalDateTime.of(2026, 4, 29, 10, 0, 0);

        // When
        LocalDateTime aligned = calculatorService.alignToNextBillingCut(refDate, 28);

        // Then: Should align to May 28, 2026 23:59:59
        assertEquals(LocalDateTime.of(2026, 5, 28, 23, 59, 59), aligned);
    }

    @Test
    @DisplayName("Should align to day 28 in current month if reference date is on or before day 28")
    void shouldAlignToCurrentMonthIfBeforeOrOnDay28() {
        // Given reference date on April 28, 2026 at 10:00
        LocalDateTime refDate = LocalDateTime.of(2026, 4, 28, 10, 0, 0);

        // When
        LocalDateTime aligned = calculatorService.alignToNextBillingCut(refDate, 28);

        // Then: Should align to April 28, 2026 23:59:59
        assertEquals(LocalDateTime.of(2026, 4, 28, 23, 59, 59), aligned);
    }

    @Test
    @DisplayName("Should calculate next period end advancing exactly one month to day 28")
    void shouldCalculateNextPeriodEndAdvancingOneMonth() {
        // Given current period end April 28, 2026 23:59:59
        LocalDateTime currentEnd = LocalDateTime.of(2026, 4, 28, 23, 59, 59);

        // When
        LocalDateTime nextEnd = calculatorService.calculateNextPeriodEnd(currentEnd, 28);

        // Then: May 28, 2026 23:59:59
        assertEquals(LocalDateTime.of(2026, 5, 28, 23, 59, 59), nextEnd);
    }
}

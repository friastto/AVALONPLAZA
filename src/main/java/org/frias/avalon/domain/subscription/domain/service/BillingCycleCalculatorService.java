package org.frias.avalon.domain.subscription.domain.service;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Domain service that calculates billing cycles aligned to a specific day of the month (default 28).
 * Guarantees a minimum 30-day trial period and a 7-day grace period post expiration.
 */
@Service
public class BillingCycleCalculatorService {

    public static final int DEFAULT_BILLING_DAY = 28;
    public static final int TRIAL_DAYS = 30;
    public static final int GRACE_DAYS = 7;

    /**
     * Calculates the initial trial and billing cut dates for a newly approved outlet.
     */
    public BillingSchedule calculateInitialSchedule(LocalDateTime startTimestamp, int billingDay) {
        LocalDateTime trialEndsAt = startTimestamp.plusDays(TRIAL_DAYS);
        LocalDateTime currentPeriodEnd = alignToNextBillingCut(trialEndsAt, billingDay);
        LocalDateTime gracePeriodEnd = currentPeriodEnd.plusDays(GRACE_DAYS);

        return new BillingSchedule(
                trialEndsAt,
                startTimestamp,
                currentPeriodEnd,
                gracePeriodEnd
        );
    }

    /**
     * Aligns a given reference date to the first billing cut date (day of month) that is on or after referenceDate.
     */
    public LocalDateTime alignToNextBillingCut(LocalDateTime referenceDate, int billingDay) {
        LocalDate refDate = referenceDate.toLocalDate();
        int day = Math.min(billingDay, 28); // 28 is universally safe across all 12 months including February

        LocalDate cutInCurrentMonth = LocalDate.of(refDate.getYear(), refDate.getMonth(), day);
        if (!cutInCurrentMonth.isBefore(refDate)) {
            return cutInCurrentMonth.atTime(23, 59, 59);
        } else {
            LocalDate nextMonth = refDate.plusMonths(1);
            LocalDate cutInNextMonth = LocalDate.of(nextMonth.getYear(), nextMonth.getMonth(), day);
            return cutInNextMonth.atTime(23, 59, 59);
        }
    }

    /**
     * Extends an active subscription from its current end date to the next month's billing cut date.
     */
    public LocalDateTime calculateNextPeriodEnd(LocalDateTime currentPeriodEnd, int billingDay) {
        LocalDate curDate = currentPeriodEnd.toLocalDate();
        LocalDate nextMonth = curDate.plusMonths(1);
        int day = Math.min(billingDay, 28);
        return LocalDate.of(nextMonth.getYear(), nextMonth.getMonth(), day).atTime(23, 59, 59);
    }

    public record BillingSchedule(
            LocalDateTime trialEndsAt,
            LocalDateTime currentPeriodStart,
            LocalDateTime currentPeriodEnd,
            LocalDateTime gracePeriodEnd
    ) {
    }
}

package org.frias.avalon.domain.company.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Domain record representing a Company in ApiAvalon.
 */
public record CompanyDomain(
        Long id,
        String nit,
        String name,
        String email,
        Long statusId,
        BigDecimal defaultCashThresholdAmount,
        Boolean policiesAccepted,
        LocalDateTime policiesAcceptedAt,
        String policiesVersion,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public CompanyDomain(
            Long id,
            String nit,
            String name,
            String email,
            Long statusId,
            BigDecimal defaultCashThresholdAmount,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this(id, nit, name, email, statusId, defaultCashThresholdAmount, false, null, "v1.0", createdAt, updatedAt);
    }

    public CompanyDomain {
        if (nit != null && nit.isBlank()) {
            throw new IllegalArgumentException("NIT cannot be blank");
        }
        if (name != null && name.isBlank()) {
            throw new IllegalArgumentException("Company name cannot be blank");
        }
    }
}

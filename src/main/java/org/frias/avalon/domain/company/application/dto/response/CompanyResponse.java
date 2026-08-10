package org.frias.avalon.domain.company.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO returned after creating or querying a Company.
 */
public record CompanyResponse(
        Long id,
        String nit,
        String name,
        String email,
        Long statusId,
        BigDecimal defaultCashThresholdAmount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

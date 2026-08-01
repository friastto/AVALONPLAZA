package org.frias.avalon.domain.company.domain.model;

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
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public CompanyDomain {
        if (nit != null && nit.isBlank()) {
            throw new IllegalArgumentException("NIT cannot be blank");
        }
        if (name != null && name.isBlank()) {
            throw new IllegalArgumentException("Company name cannot be blank");
        }
    }
}

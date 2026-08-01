package org.frias.avalon.domain.company.application.dto.response;

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
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

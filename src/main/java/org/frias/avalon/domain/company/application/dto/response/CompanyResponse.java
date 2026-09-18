package org.frias.avalon.domain.company.application.dto.response;

import org.frias.avalon.domain.company.domain.model.CompanyDomain;
import org.frias.avalon.domain.masterdata.application.dto.response.MasterRefDto;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;

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
        MasterRefDto status,
        BigDecimal defaultCashThresholdAmount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public CompanyResponse(
            Long id,
            String nit,
            String name,
            String email,
            Long statusId,
            BigDecimal defaultCashThresholdAmount,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this(id, nit, name, email, statusId, null, defaultCashThresholdAmount, createdAt, updatedAt);
    }

    public static CompanyResponse from(CompanyDomain domain, MasterTree tree) {
        if (domain == null) return null;
        MasterRoot node = (domain.statusId() != null && tree != null) ? tree.getById(domain.statusId()) : null;
        MasterRefDto ref = (node != null) ? MasterRefDto.from(node) : null;
        return new CompanyResponse(
                domain.id(),
                domain.nit(),
                domain.name(),
                domain.email(),
                domain.statusId(),
                ref,
                domain.defaultCashThresholdAmount(),
                domain.createdAt(),
                domain.updatedAt()
        );
    }
}

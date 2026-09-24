package org.frias.avalon.domain.company.application.dto.response;

import org.frias.avalon.domain.masterdata.application.dto.response.MasterRefDto;

import java.time.LocalDateTime;

/**
 * Detailed DTO for SuperAdmin review of a Company Service Request.
 */
public record CompanyServiceRequestDetailResponse(
        Long companyId,
        String companyName,
        String companyNit,
        String companyEmail,
        Long statusId,
        MasterRefDto status,
        LocalDateTime createdAt,
        Long outletId,
        String storeName,
        String storeAddress,
        String storePhone,
        Double latitude,
        Double longitude,
        Long applicantUserId,
        String applicantFullName,
        String applicantIdentification,
        String applicantEmail,
        String applicantPhone
) {
}

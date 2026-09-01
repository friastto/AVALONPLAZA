package org.frias.avalon.domain.company.application.dto.response;

import java.time.LocalDateTime;

public record CompanyManagerResponse(
        Long assignmentId,
        Long companyId,
        Long userId,
        Long personId,
        String identificationNumber,
        String fullName,
        String email,
        String phone,
        String username,
        Long roleId,
        String roleCode,
        Long statusId,
        LocalDateTime assignedAt
) {}

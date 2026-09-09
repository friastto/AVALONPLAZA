package org.frias.avalon.domain.company.application.dto.response;

public record CompanyEmployeeResponse(
        Long userId,
        String userName,
        Long personId,
        String name,
        String lastName,
        String numberId,
        String email,
        Long phoneNumber,
        Long roleId,
        String roleCode,
        String roleName,
        String roleCategory,
        Long outletId,
        String outletName,
        Long statusId,
        String statusName
) {
}

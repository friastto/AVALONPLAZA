package org.frias.avalon.domain.user.application.dtos.request;

public record AssignmentRoleRequestDto(
        Long userId,
        Long roleId,
        String roleCode,
        Long outletId
) {
    public AssignmentRoleRequestDto(Long userId, Long roleId, Long outletId) {
        this(userId, roleId, null, outletId);
    }
}

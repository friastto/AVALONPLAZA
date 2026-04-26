package org.frias.avalon.domain.user.application.dtos.request;

public record AssignmentRoleRequestDto (
        Long userId,
        Long roleId,
        Long staffScopeId,
        Long scope

){
}

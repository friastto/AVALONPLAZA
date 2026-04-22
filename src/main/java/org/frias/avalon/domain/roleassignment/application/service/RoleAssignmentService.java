package org.frias.avalon.domain.roleassignment.application.service;

import org.frias.avalon.domain.roleassignment.domain.entity.RoleAssignment;

public interface RoleAssignmentService {
    RoleAssignment searchById(Long id);
    RoleAssignment save(
                        Long userId,
                        Long roleId,
                        Long staffScopeId,
                        Long scopeId,
                        Long scheduleId,
                        Long statusId
    );
}

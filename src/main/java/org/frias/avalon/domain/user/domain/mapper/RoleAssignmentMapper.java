package org.frias.avalon.domain.user.domain.mapper;

import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.user.application.dtos.response.AssignmentRoleResponse;
import org.frias.avalon.domain.user.domain.model.RoleAssignmentDomain;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.frias.avalon.domain.user.infraestruture.persistence.entity.RoleAssignment;

public interface RoleAssignmentMapper {

    RoleAssignment toEntity(RoleAssignmentDomain domain);
    RoleAssignmentDomain toDomain(RoleAssignment role);
    AssignmentRoleResponse toResponse(RoleAssignmentDomain domain);


    AssignmentRoleResponse toResponse(
            UserAvalonDomain user,
            MasterRoot userStatus,
            MasterRoot role,
            MasterRoot statusActive,
            Long outlet
    );
}

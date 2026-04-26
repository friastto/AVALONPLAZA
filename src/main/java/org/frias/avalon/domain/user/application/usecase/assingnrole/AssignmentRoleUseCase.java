package org.frias.avalon.domain.user.application.usecase.assingnrole;

import org.frias.avalon.domain.user.application.dtos.request.AssignmentRoleRequestDto;
import org.frias.avalon.domain.user.application.dtos.response.AssignmentRoleResponse;
import org.frias.avalon.domain.user.domain.model.RoleAssignmentDomain;

public interface AssignmentRoleUseCase {

    AssignmentRoleResponse execute(AssignmentRoleRequestDto request);

}

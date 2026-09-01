package org.frias.avalon.domain.user.domain.port;

import org.frias.avalon.domain.user.domain.model.RoleAssignmentDomain;

import java.util.List;

public interface RoleAssignmentRepositoryPort {

    RoleAssignmentDomain create(RoleAssignmentDomain domain);

    List<RoleAssignmentDomain> findByUserAvalonId(Long id);

    List<RoleAssignmentDomain> findByOutletId(Long outletId);

    RoleAssignmentDomain update(RoleAssignmentDomain domain);

    List<RoleAssignmentDomain> findByCompanyId(Long companyId);

    java.util.Optional<RoleAssignmentDomain> findByCompanyIdAndRoleId(Long companyId, Long roleId);
}

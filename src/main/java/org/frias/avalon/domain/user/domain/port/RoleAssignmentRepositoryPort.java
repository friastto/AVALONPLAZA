package org.frias.avalon.domain.user.domain.port;

import org.frias.avalon.domain.user.domain.model.RoleAssignmentDomain;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;

import java.util.List;

public interface RoleAssignmentRepositoryPort {

    RoleAssignmentDomain create(RoleAssignmentDomain domain);

    List<RoleAssignmentDomain> findByUser(Long id);

}

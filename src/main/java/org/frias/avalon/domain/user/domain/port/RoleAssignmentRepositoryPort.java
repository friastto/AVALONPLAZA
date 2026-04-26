package org.frias.avalon.domain.user.domain.port;

import org.frias.avalon.domain.user.domain.model.RoleAssignmentDomain;

public interface RoleAssignmentRepositoryPort {

    RoleAssignmentDomain create(RoleAssignmentDomain domain);
}

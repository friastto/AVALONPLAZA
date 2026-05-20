package org.frias.avalon.domain.user.application.service;

import org.frias.avalon.domain.outlet.domain.model.OutletDomain;
import org.frias.avalon.domain.user.domain.model.RoleAssignmentDomain;
import org.springframework.stereotype.Service;

import java.util.List;


public interface UserAvalonOutletResolverService {
    OutletDomain resolveActiveOutlet(List<RoleAssignmentDomain> roleAssigned);
}

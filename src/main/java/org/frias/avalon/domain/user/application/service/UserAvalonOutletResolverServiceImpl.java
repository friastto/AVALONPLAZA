package org.frias.avalon.domain.user.application.service;

import lombok.AllArgsConstructor;
import org.frias.avalon.core.exeptions.BusinessException;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.outlet.domain.model.OutletDomain;
import org.frias.avalon.domain.outlet.domain.port.OutletRepositoryPort;
import org.frias.avalon.domain.user.domain.model.RoleAssignmentDomain;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UserAvalonOutletResolverServiceImpl implements UserAvalonOutletResolverService{
    private final OutletRepositoryPort outletPort;
    private final MasterTreeProvider masterTreeProvider;


    @Override
    public OutletDomain resolveActiveOutlet(List<RoleAssignmentDomain> roleAssigned) {
        MasterTree tree = masterTreeProvider.getTree();

        RoleAssignmentDomain employeeAssignment = roleAssigned.stream()
                .filter(assignment ->
                        assignment.getOutletId() != null
                                && tree.isChildOf(tree.getById(assignment.getRoleId()), "EMP")
                                && tree.is(tree.getById(assignment.getStatus()), "ACT")
                )
                .findFirst()
                .orElse(null);

        if (employeeAssignment != null) {
            return outletPort.findById(employeeAssignment.getOutletId())
                    .orElseThrow(() -> new BusinessException("Este usuario tiene un perfil de empleado pero no está asignado a una tienda dentro de avalon"));
        }
        return null;
    }
}

package org.frias.avalon.domain.user.application.service;

import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.user.domain.model.RoleAssignmentDomain;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.frias.avalon.domain.user.domain.port.RoleAssignmentRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ExtractRoleAssignedService {
    private final RoleAssignmentRepositoryPort rolePort;
    private final MasterTreeProvider treeProvider;

    public ExtractRoleAssignedService(RoleAssignmentRepositoryPort rolePort, MasterTreeProvider treeProvider) {
        this.rolePort = rolePort;
        this.treeProvider = treeProvider;
    }

    public  List<RoleAssignmentDomain> extract(UserAvalonDomain user){
       return rolePort.findByUser(user.getId());
    }
    public List<String> extract(List<RoleAssignmentDomain> rolesDomain){

        MasterTree tree = treeProvider.getTree();

        return rolesDomain.stream().map(rol-> tree.getById(rol.getRoleId()).getShortName()).toList();

    }


}

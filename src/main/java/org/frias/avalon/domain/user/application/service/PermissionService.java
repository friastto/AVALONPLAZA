package org.frias.avalon.domain.user.application.service;

import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class PermissionService {

    public List<String> resolvePermissions(List<MasterRoot> roles) {

        Set<String> permissions = new HashSet<>();

        for (MasterRoot role : roles) {

            permissions.addAll(permisos(role));
        }

        return new ArrayList<>(permissions);
    }
    public List<String> resolvePermissions(MasterRoot role) {

        return new ArrayList<>(permisos(role));
    }
    private Set<String> permisos(MasterRoot role){
        Set<String> permissions = new HashSet<>();
        switch (role.getShortName()) {

            case "GERGEN":
                permissions.add("VIEW_DASHBOARD");
                permissions.add("MANAGE_EMPLOYEE");
                permissions.add("POS_SALES");
                permissions.add("MANAGE_INVENTORY");

                break;

            case "CJTURNO":
                permissions.add("POS_SALES");
                break;

            case "CSTNDR":
                permissions.add("VIEW_MARKETPLACE");
                permissions.add("BUY_PRODUCTS");
                break;

            case "USANONIMO":
                permissions.add("VIEW_MARKETPLACE");
                break;
        }

        return permissions;
    }




    /**
     * cuando  coloque en el arbol de permisos enmasterdata de permisos por rol
     */
    /*
    @Component
public class PermissionService {

    private final MasterTreeProvider treeProvider;

    public PermissionService(MasterTreeProvider treeProvider) {
        this.treeProvider = treeProvider;
    }

    public List<String> resolvePermissions(List<MasterRoot> roles) {

        var tree = treeProvider.getTree();

        return roles.stream()
                .flatMap(role -> tree.getChildren(role).stream())
                .map(MasterRoot::getShortName)
                .distinct()
                .toList();
    }
}
     */
}
package org.frias.avalon.domain.user.domain.model;

import lombok.Getter;
import lombok.Setter;


@Getter
public class RoleAssignmentDomain {

    Long id;

    Long userId;

    Long roleId;

    Long staffScope;

    Long scope;

    // Long schedule;

    Long status;

    public RoleAssignmentDomain(Long id, Long userId, Long roleId, Long staffScope, Long scope, Long status) {
        this.id = id;
        this.userId = userId;
        this.roleId = roleId;
        this.staffScope = staffScope;
        this.scope = scope;
        this.status = status;
    }
    public RoleAssignmentDomain(Long userId, Long roleId, Long staffScope, Long scope, Long status) {

        this.userId = userId;
        this.roleId = roleId;
        this.staffScope = staffScope;
        this.scope = scope;
        this.status = status;
    }

    public static RoleAssignmentDomain create(Long userId,Long roleId, Long staffScopeId, Long scopeId, Long statusId){

        if (userId == null || roleId == null || statusId == null) {
            throw new IllegalArgumentException("Campos obligatorios faltantes");
        }

        if (staffScopeId != null && scopeId == null) {
            throw new IllegalStateException("staffScope requiere scope");
        }

        return new RoleAssignmentDomain(
                userId,
                roleId,
                staffScopeId,
                scopeId,
                statusId
        );




    }



}

package org.frias.avalon.domain.user.domain.model;

import lombok.Getter;


@Getter
public class RoleAssignmentDomain {

    Long id;

    Long userId;

    Long roleId;

    Long outletId;

    // Long schedule;

    Long status;

    public RoleAssignmentDomain(Long id, Long userId, Long roleId, Long outletId, Long status) {
        this.id = id;
        this.userId = userId;
        this.roleId = roleId;
        this.outletId = outletId;

        this.status = status;
    }
    public RoleAssignmentDomain(Long userId, Long roleId, Long outletId,Long status) {

        this.userId = userId;
        this.roleId = roleId;
        this.outletId = outletId;

        this.status = status;
    }

    public static RoleAssignmentDomain create(Long userId,Long roleId, Long outletId, Long statusId){

        if (userId == null || roleId == null || statusId == null) {
            throw new IllegalArgumentException("Campos obligatorios faltantes");
        }

        return new RoleAssignmentDomain(
                userId,
                roleId,
                outletId,
                statusId
        );




    }



}

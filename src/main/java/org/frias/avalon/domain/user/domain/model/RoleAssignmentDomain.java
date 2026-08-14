package org.frias.avalon.domain.user.domain.model;

/**
 * Pure Java Domain model representing a Role Assignment in ApiAvalon.
 * Free of Lombok annotations.
 */
public class RoleAssignmentDomain {

    private Long id;
    private Long userId;
    private Long roleId;
    private Long outletId;
    private Long status;

    public RoleAssignmentDomain(Long id, Long userId, Long roleId, Long outletId, Long status) {
        this.id = id;
        this.userId = userId;
        this.roleId = roleId;
        this.outletId = outletId;
        this.status = status;
    }

    public RoleAssignmentDomain(Long userId, Long roleId, Long outletId, Long status) {
        this.userId = userId;
        this.roleId = roleId;
        this.outletId = outletId;
        this.status = status;
    }

    public static RoleAssignmentDomain create(Long userId, Long roleId, Long outletId, Long statusId) {
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

    public void changeStatus(Long newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("El estado no puede ser nulo");
        }
        this.status = newStatus;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getRoleId() { return roleId; }
    public Long getOutletId() { return outletId; }
    public Long getStatus() { return status; }
}

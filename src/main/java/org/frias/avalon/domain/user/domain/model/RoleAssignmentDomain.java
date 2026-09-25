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
    private Long companyId;
    private Long status;

    public RoleAssignmentDomain(Long id, Long userId, Long roleId, Long outletId, Long companyId, Long status) {
        this.id = id;
        this.userId = userId;
        this.roleId = roleId;
        this.outletId = outletId;
        this.companyId = companyId;
        this.status = status;
    }

    public RoleAssignmentDomain(Long id, Long userId, Long roleId, Long outletId, Long status) {
        this(id, userId, roleId, outletId, null, status);
    }

    public RoleAssignmentDomain(Long userId, Long roleId, Long outletId, Long status) {
        this(null, userId, roleId, outletId, null, status);
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

    public static RoleAssignmentDomain createCompanyRole(Long userId, Long roleId, Long companyId, Long statusId) {
        if (userId == null || roleId == null || companyId == null || statusId == null) {
            throw new IllegalArgumentException("Campos obligatorios faltantes para rol de empresa");
        }

        return new RoleAssignmentDomain(
                null,
                userId,
                roleId,
                null,
                companyId,
                statusId
        );
    }

    public void changeStatus(Long newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("El estado no puede ser nulo");
        }
        this.status = newStatus;
    }

    public void changeOutlet(Long newOutletId) {
        this.outletId = newOutletId;
    }

    public void changeCompany(Long newCompanyId) {
        this.companyId = newCompanyId;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getRoleId() { return roleId; }
    public Long getOutletId() { return outletId; }
    public Long getCompanyId() { return companyId; }
    public Long getStatus() { return status; }
}

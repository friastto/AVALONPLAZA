package org.frias.avalon.domain.masterdata.domain.model;

import org.frias.avalon.core.exeptions.DomainValidationException;
public class MasterRoot {
    private final Long id;
    private final String shortName;
    private final String fullName;
    private final Long parentId;
    private Long statusId;

    public MasterRoot(Long id, String shortName, String fullName, Long parentId, Long statusId) {
        this.id = id;
        this.shortName = shortName;
        this.fullName = fullName;
        this.parentId = parentId;
        this.statusId = statusId;
    }

    public static MasterRoot create(String shortName, String fullName, Long parentId, Long statusId) {


        if (shortName == null || shortName.isBlank()) {
            throw new RuntimeException("shortName requerido");
        }
        if (fullName == null || fullName.isBlank()) {
            throw new RuntimeException("fullName requerido");
        }

        return new MasterRoot(null, shortName.toUpperCase(), fullName.toUpperCase(), parentId, statusId);

    }

    public static MasterRoot fromPersistence(
            Long id,
            String shortName,
            String fullName,
            Long parentId,
            Long statusId
    ) {

        if (shortName == null || shortName.isBlank()) {
            throw new IllegalStateException("Data corrupta en BD: shortName null");
        }
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalStateException("Data corrupta en BD: fullName null");
        }
        if (id == null) {
            throw new IllegalStateException("Data corrupta en BD: id null " + shortName);
        }

        return new MasterRoot(id, shortName, fullName, parentId, statusId);
    }

    public void changeStatus(Long newStatusId) {

        if (newStatusId == null) {
            throw new DomainValidationException("Status requerido");
        }

        if (this.statusId.equals(newStatusId)) {
            throw new DomainValidationException("Ya tiene ese status");
        }

        this.statusId = newStatusId;
    }

    public boolean is(String code) {
        return this.shortName.equals(code);
    }

    public boolean isActive(String statusCode) {
        return "ACT".equals(statusCode);
    }

    public boolean canDisable(String statusCode) {
        return "ACT".equals(statusCode)
                && !"INACT".equals(statusCode)
                && !"BLOK".equals(statusCode);
    }

    public Long getId() {
        return id;
    }

    public String getShortName() {
        return shortName;
    }

    public String getFullName() {
        return fullName;
    }

    public Long getParentId() {
        return parentId;
    }

    public Long getStatusId() {
        return statusId;
    }
}
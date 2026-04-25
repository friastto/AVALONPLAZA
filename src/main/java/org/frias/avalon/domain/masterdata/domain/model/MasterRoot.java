package org.frias.avalon.domain.masterdata.domain.model;

import lombok.Getter;
import org.frias.avalon.core.exeptions.DomainValidationException;

@Getter
public class MasterRoot {
    private Long id;
    private String fullName;
    private String shortName;
    private Long parentId;
    private Long statusId;

    public MasterRoot(Long id, String fullName, String shortName, Long parentId, Long statusId) {
        this.id = id;
        this.fullName = fullName;
        this.shortName = shortName;
        this.parentId = parentId;
        this.statusId = statusId;
    }

    public static MasterRoot create(String fullName, String shortName, Long parentId, Long statusId){



        if (shortName == null || shortName.isBlank()) {
            throw new RuntimeException("shortName requerido");
        }
        if (fullName == null || fullName.isBlank()) {
            throw new RuntimeException("fullName requerido");
        }

        return new MasterRoot(null, fullName, shortName, parentId, statusId);

    }

    public static MasterRoot fromPersistence(
            Long id,
            String fullName,
            String shortName,
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
            throw new IllegalStateException("Data corrupta en BD: id null");
        }

        return new MasterRoot(id, fullName, shortName, parentId, statusId);
    }

    public void changeStatus(Long newStatusId){

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

    public boolean canDisable(String statusCode){
        return "ACT".equals(statusCode)
                && !"INACT".equals(statusCode)
                && !"BLOK".equals(statusCode);
    }

}

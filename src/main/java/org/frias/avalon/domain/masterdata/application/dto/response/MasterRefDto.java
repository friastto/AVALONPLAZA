package org.frias.avalon.domain.masterdata.application.dto.response;

import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;

public record MasterRefDto(
    Long id,
    String code,
    String name
) {
    public static MasterRefDto from(MasterRoot node) {
        if (node == null) return null;
        return new MasterRefDto(node.getId(), node.getShortName(), node.getFullName());
    }
}

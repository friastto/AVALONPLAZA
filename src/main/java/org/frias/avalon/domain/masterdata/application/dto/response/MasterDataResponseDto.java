package org.frias.avalon.domain.masterdata.application.dto.response;

public record MasterDataResponseDto(
        Long id,
        String shortName,
        String fullName,
        Long parentId,
        String statusCode
) {
    public MasterDataResponseDto(Long id, String shortName, String fullName) {
        this(id, shortName, fullName, null, "ACTIVO");
    }
}

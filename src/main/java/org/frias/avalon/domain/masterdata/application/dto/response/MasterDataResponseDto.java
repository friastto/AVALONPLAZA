package org.frias.avalon.domain.masterdata.application.dto.response;

public record MasterDataResponseDto(
        Long id,
        String fullName,
        String shortName
) {
}

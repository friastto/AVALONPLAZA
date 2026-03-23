package org.frias.avalon.domain.masterdata.dtos.response;

public record MasterDataResponseDto(
        Long id,
        String fullName,
        String shortName
) {}

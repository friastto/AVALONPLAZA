package org.frias.avalon.domain.person.application.dto.response;

import org.frias.avalon.domain.masterdata.application.dto.response.MasterDataResponseDto; // Importar MasterDataResponseDto
import java.time.LocalDateTime;

public record PersonResponse(
        Long id,
        String numberid,
        String name,
        String lastName,
        String address,
        MasterDataResponseDto typeIdentification, // Cambiado de Long a MasterDataResponseDto
        MasterDataResponseDto sex,                // Cambiado de Long a MasterDataResponseDto
        Long phoneNumber,
        String email,
        MasterDataResponseDto status,             // Cambiado de Long a MasterDataResponseDto
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
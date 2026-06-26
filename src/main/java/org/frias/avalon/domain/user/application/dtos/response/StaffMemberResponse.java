package org.frias.avalon.domain.user.application.dtos.response;

import org.frias.avalon.domain.masterdata.application.dto.response.MasterDataResponseDto;
import org.frias.avalon.domain.masterdata.application.dto.response.StatusResponseDto;

public record StaffMemberResponse(
        Long userId,
        String userName,
        Long personId,
        String name,
        String lastName,
        String numberId,
        String email,
        Long phoneNumber,
        String address,
        Long sexId,
        Long typeIdentificationId,
        MasterDataResponseDto role,
        StatusResponseDto status
) {
}

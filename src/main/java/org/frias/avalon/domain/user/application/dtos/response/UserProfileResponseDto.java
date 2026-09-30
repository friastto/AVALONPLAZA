package org.frias.avalon.domain.user.application.dtos.response;

import org.frias.avalon.domain.masterdata.application.dto.response.MasterRefDto;

import java.util.List;

public record UserProfileResponseDto(
        Long userId,
        String userName,
        Long personId,
        String name,
        String lastName,
        String fullName,
        String email,
        Long phoneNumber,
        String address,
        MasterRefDto identificationType,
        String numberId,
        MasterRefDto status,
        List<MasterRefDto> roles,
        MasterRefDto activeRole
) {
}

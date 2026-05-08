package org.frias.avalon.domain.user.application.dtos.response;

import org.frias.avalon.domain.masterdata.application.dto.response.MasterDataResponseDto;
import org.frias.avalon.domain.masterdata.application.dto.response.StatusResponseDto;

public record AssignmentRoleResponse(
        UserAvalonResponseDto userAvalon,
        MasterDataResponseDto role,
        Long outlet,
        StatusResponseDto status
) {
}

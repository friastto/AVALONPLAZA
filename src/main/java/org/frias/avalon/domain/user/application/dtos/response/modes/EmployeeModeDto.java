package org.frias.avalon.domain.user.application.dtos.response.modes;

import org.frias.avalon.domain.masterdata.application.dto.response.MasterDataResponseDto;
import org.frias.avalon.domain.outlet.application.dto.response.OutletInfoDto;

import java.util.List;

public record EmployeeModeDto(
        boolean enabled,
        OutletInfoDto store,
        MasterDataResponseDto role,
        List<String> permissions
) {
}

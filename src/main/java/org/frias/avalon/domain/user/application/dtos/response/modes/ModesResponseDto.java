package org.frias.avalon.domain.user.application.dtos.response.modes;

public record ModesResponseDto(
        ClientModeDto client,
        EmployeeModeDto employee,
        AdminAvalonModeDto  adminAvalon
) {
}

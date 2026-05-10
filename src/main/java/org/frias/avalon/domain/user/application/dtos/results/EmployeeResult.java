package org.frias.avalon.domain.user.application.dtos.results;

import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.outlet.application.dto.response.OutletInfoDto;

import java.util.List;

public record EmployeeResult(
        boolean status,
        OutletInfoDto outlet,
        MasterRoot role,
        List<String> permissions

) {
}

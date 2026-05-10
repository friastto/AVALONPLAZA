package org.frias.avalon.domain.user.application.service;

import org.frias.avalon.domain.outlet.domain.model.OutletDomain;
import org.frias.avalon.domain.user.application.dtos.response.modes.ModesResponseDto;
import org.frias.avalon.domain.user.application.dtos.results.ModesResult;
import org.frias.avalon.domain.user.domain.model.RoleAssignmentDomain;

import java.util.List;

public interface ModesMachine {

    ModesResult resolve(List<RoleAssignmentDomain> byUser, OutletDomain outletDomain);
    ModesResponseDto mapperToResponse(ModesResult mode);

}

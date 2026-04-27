package org.frias.avalon.domain.outlet.infraestructure.mapper;

import org.frias.avalon.domain.outlet.application.dto.response.OutletResponseDto;
import org.frias.avalon.domain.outlet.domain.model.OutletDomain;
import org.frias.avalon.domain.outlet.infraestructure.entities.Outlet;

public interface OutletMapper {

    OutletDomain toDomain(Outlet o);
    Outlet toEntity(OutletDomain od);
    OutletResponseDto toResponse(OutletDomain od);



}

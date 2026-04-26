package org.frias.avalon.domain.user.domain.mapper;

import org.frias.avalon.domain.masterdata.application.dto.response.MasterDataResponseDto;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.user.application.dtos.response.UserAvalonResponseDto;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.frias.avalon.domain.user.infraestruture.persistence.entity.UserAvalon;

public interface UserAvalonMapper {

    UserAvalonDomain toDomain(UserAvalon entity);
    UserAvalon toEntity(UserAvalonDomain dominio);
    UserAvalonResponseDto toResponse(UserAvalonDomain domain, MasterRoot status);
}

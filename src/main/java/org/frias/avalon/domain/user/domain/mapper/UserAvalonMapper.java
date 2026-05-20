package org.frias.avalon.domain.user.domain.mapper;

import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.person.domain.model.PersonDomain;
import org.frias.avalon.domain.user.application.dtos.response.UserAvalonDto;
import org.frias.avalon.domain.user.application.dtos.response.UserAvalonResponseDto;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.frias.avalon.domain.user.infraestructure.persistence.entity.UserAvalon;

public interface UserAvalonMapper {

    UserAvalonDomain toDomain(UserAvalon entity);
    UserAvalonDomain toDomainAdvance(UserAvalon entity);
    UserAvalon toEntity(UserAvalonDomain dominio);
    UserAvalonResponseDto toResponse(UserAvalonDomain domain, MasterRoot status);
    UserAvalonDto toResponseWithPersonData(UserAvalonDomain domain, PersonDomain personData, MasterRoot status);
}

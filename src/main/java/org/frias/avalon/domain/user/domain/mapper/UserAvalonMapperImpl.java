package org.frias.avalon.domain.user.domain.mapper;

import org.frias.avalon.domain.masterdata.application.dto.response.StatusResponseDto;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.user.application.dtos.response.UserAvalonResponseDto;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.frias.avalon.domain.user.infraestruture.persistence.entity.UserAvalon;
import org.springframework.stereotype.Component;

@Component
public class UserAvalonMapperImpl implements UserAvalonMapper {
    @Override
    public UserAvalonDomain toDomain(UserAvalon entity) {

        return UserAvalonDomain.fromPersistenceBasic(
                entity.getId(),
                entity.getUserName(),
                entity.getStatusId()

        );

    }

    @Override
    public UserAvalon toEntity(UserAvalonDomain dominio) {

        UserAvalon ua = new UserAvalon();

        ua.setId(dominio.getId());
        ua.setUserName(dominio.getUserName());
        ua.setHashSalt(dominio.getHashSalt());
        ua.setHashPassword(dominio.getHashPassword());
        ua.setStatusId(dominio.getStatusId());


        return ua;
    }

    @Override
    public UserAvalonResponseDto toResponse(UserAvalonDomain domain, MasterRoot statusRoot) {

        StatusResponseDto status = new StatusResponseDto(
                statusRoot.getId(),
                statusRoot.getShortName(),
                statusRoot.getFullName()
        );
        return new UserAvalonResponseDto(
                domain.getId(),
                domain.getUserName(),
                status


        );
    }


    public UserAvalonResponseDto toDto(UserAvalonDomain domain) {


        return null;
    }
}

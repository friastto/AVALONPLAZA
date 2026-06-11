package org.frias.avalon.domain.user.domain.mapper;

import org.frias.avalon.domain.masterdata.application.dto.response.StatusResponseDto;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.person.domain.model.PersonDomain;
import org.frias.avalon.domain.user.application.dtos.response.UserAvalonDto;
import org.frias.avalon.domain.user.application.dtos.response.UserAvalonResponseDto;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.frias.avalon.domain.user.infraestructure.persistence.entity.UserAvalon;
import org.springframework.stereotype.Component;

@Component
public class UserAvalonMapperImpl implements UserAvalonMapper {
    private final MasterTreeProvider masterTreeProvider;

    public UserAvalonMapperImpl(MasterTreeProvider masterTreeProvider) {
        this.masterTreeProvider = masterTreeProvider;
    }

    @Override
    public UserAvalonDomain toDomain(UserAvalon entity) {

        return UserAvalonDomain.fromPersistenceAdvanced(
                entity.getId(),
                entity.getPersonId(),
                entity.getUserName(),
                entity.getHashSalt(),
                entity.getHashPassword(),
                entity.getStatusId()

        );

    }

    @Override
    public UserAvalonDomain toDomainAdvance(UserAvalon entity) {

        return UserAvalonDomain.fromPersistenceAdvanced(
                entity.getId(),
                entity.getPersonId(),
                entity.getUserName(),
                entity.getHashSalt(),
                entity.getHashPassword(),
                entity.getStatusId()

        );

    }

    @Override
    public UserAvalon toEntity(UserAvalonDomain dominio) {

        UserAvalon ua = new UserAvalon();

        ua.setId(dominio.getId());
        ua.setPersonId(dominio.getPersonId());
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
                domain.getPersonId(),
                domain.getUserName(),
                status


        );
    }

    @Override
    public UserAvalonDto toResponseWithPersonData(UserAvalonDomain domain, PersonDomain personData, MasterRoot statusRoot) {


        MasterTree tree = masterTreeProvider.getTree();


        return new UserAvalonDto(
                domain.getId(),
                tree.getById(personData.getTypeIdentificationId()).getFullName(),
                personData.getNumberid(),
                domain.getUserName(),
                "Cliente Estandar",
                personData.getFullName(),
                personData.getAddress(),
                tree.getById(personData.getSexId()).getFullName(),
                tree.getById(personData.getStatusId()).getFullName()
        );
    }


    public UserAvalonResponseDto toDto(UserAvalonDomain domain) {


        return null;
    }
}

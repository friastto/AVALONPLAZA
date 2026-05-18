package org.frias.avalon.domain.user.domain.mapper;

import org.frias.avalon.domain.masterdata.application.dto.response.MasterDataResponseDto;
import org.frias.avalon.domain.masterdata.application.dto.response.StatusResponseDto;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.infraestructure.mapper.MasterDataMapperService;
import org.frias.avalon.domain.user.application.dtos.response.AssignmentRoleResponse;
import org.frias.avalon.domain.user.application.dtos.response.UserAvalonResponseDto;
import org.frias.avalon.domain.user.domain.model.RoleAssignmentDomain;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.frias.avalon.domain.user.infraestructure.persistence.entity.RoleAssignment;
import org.springframework.stereotype.Component;

@Component
public class RoleAssignmentMapperImpl implements RoleAssignmentMapper{

    private final UserAvalonMapper userMapper;
    private final MasterDataMapperService masterMapper;

    public RoleAssignmentMapperImpl(UserAvalonMapper userMapper, MasterDataMapperService masterMapper) {
        this.userMapper = userMapper;
        this.masterMapper = masterMapper;
    }

    @Override
    public RoleAssignment toEntity(RoleAssignmentDomain domain) {

        RoleAssignment role = new RoleAssignment();
        role.setRoleId(domain.getId());
        role.setUserId(domain.getUserId());
        role.setRoleId(domain.getRoleId());
        role.setOutletId(domain.getOutletId());
        role.setStatus(domain.getStatus());


        return role;
    }

    @Override
    public RoleAssignmentDomain toDomain(RoleAssignment role) {

        return new RoleAssignmentDomain(
                role.getId(),
                role.getUserId(),
                role.getRoleId(),
                role.getOutletId(),
                role.getStatus()
        );
    }

    @Override
    public AssignmentRoleResponse toResponse(RoleAssignmentDomain domain) {


        return null;
    }

    @Override
    public AssignmentRoleResponse toResponse(UserAvalonDomain user, MasterRoot userStatus, MasterRoot role, MasterRoot statusActive, Long outletId) {

       UserAvalonResponseDto userDto = userMapper.toResponse(user,userStatus);

        MasterDataResponseDto rolevalid = masterMapper.toResponse(role);

        return new AssignmentRoleResponse(
                userDto,
                rolevalid,
                outletId,
                new StatusResponseDto(statusActive.getId(),statusActive.getShortName(),statusActive.getFullName())

        );
    }


}

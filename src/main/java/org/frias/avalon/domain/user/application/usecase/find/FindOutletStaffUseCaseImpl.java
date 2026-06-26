package org.frias.avalon.domain.user.application.usecase.find;

import org.frias.avalon.domain.masterdata.application.dto.response.MasterDataResponseDto;
import org.frias.avalon.domain.masterdata.application.dto.response.StatusResponseDto;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.repository.MasterDataRepositoryPort;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.masterdata.infraestructure.mapper.MasterDataMapperService;
import org.frias.avalon.domain.person.domain.model.PersonDomain;
import org.frias.avalon.domain.person.domain.port.PersonRepositoryPort;
import org.frias.avalon.domain.user.application.dtos.response.StaffMemberResponse;
import org.frias.avalon.domain.user.domain.model.RoleAssignmentDomain;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.frias.avalon.domain.user.domain.port.RoleAssignmentRepositoryPort;
import org.frias.avalon.domain.user.domain.port.UserAvalonRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FindOutletStaffUseCaseImpl implements FindOutletStaffUseCase {

    private final RoleAssignmentRepositoryPort roleAssignmentRepositoryPort;
    private final UserAvalonRepositoryPort userAvalonRepositoryPort;
    private final PersonRepositoryPort personRepositoryPort;
    private final MasterDataRepositoryPort masterDataRepositoryPort;
    private final MasterTreeProvider masterTreeProvider;
    private final MasterDataMapperService masterMapper;

    public FindOutletStaffUseCaseImpl(
            RoleAssignmentRepositoryPort roleAssignmentRepositoryPort,
            UserAvalonRepositoryPort userAvalonRepositoryPort,
            PersonRepositoryPort personRepositoryPort,
            MasterDataRepositoryPort masterDataRepositoryPort,
            MasterTreeProvider masterTreeProvider,
            MasterDataMapperService masterMapper) {
        this.roleAssignmentRepositoryPort = roleAssignmentRepositoryPort;
        this.userAvalonRepositoryPort = userAvalonRepositoryPort;
        this.personRepositoryPort = personRepositoryPort;
        this.masterDataRepositoryPort = masterDataRepositoryPort;
        this.masterTreeProvider = masterTreeProvider;
        this.masterMapper = masterMapper;
    }

    @Override
    public List<StaffMemberResponse> execute(Long outletId) {
        List<RoleAssignmentDomain> assignments = roleAssignmentRepositoryPort.findByOutletId(outletId);
        List<StaffMemberResponse> staffList = new ArrayList<>();
        MasterTree tree = masterTreeProvider.getTree();

        for (RoleAssignmentDomain assignment : assignments) {
            Optional<UserAvalonDomain> userOpt = userAvalonRepositoryPort.findById(assignment.getUserId());
            if (userOpt.isPresent()) {
                UserAvalonDomain user = userOpt.get();
                PersonDomain person = null;
                if (user.getPersonId() != null) {
                    person = personRepositoryPort.findById(user.getPersonId()).orElse(null);
                }

                MasterRoot roleRoot = tree.getById(assignment.getRoleId());
                MasterRoot statusRoot = tree.getById(assignment.getStatus());

                MasterDataResponseDto roleDto = roleRoot != null ? masterMapper.toResponse(roleRoot) : null;
                StatusResponseDto statusDto = statusRoot != null ? 
                        new StatusResponseDto(statusRoot.getId(), statusRoot.getShortName(), statusRoot.getFullName()) : null;

                staffList.add(new StaffMemberResponse(
                        user.getId(),
                        user.getUserName(),
                        person != null ? person.getId() : null,
                        person != null ? person.getName() : "SIN NOMBRE",
                        person != null ? person.getLastName() : "SIN APELLIDO",
                        person != null ? person.getNumberid() : "",
                        person != null ? person.getEmail() : "",
                        person != null ? person.getPhoneNumber() : null,
                        person != null ? person.getAddress() : "",
                        person != null ? person.getSexId() : null,
                        person != null ? person.getTypeIdentificationId() : null,
                        roleDto,
                        statusDto
                ));
            }
        }
        return staffList;
    }
}

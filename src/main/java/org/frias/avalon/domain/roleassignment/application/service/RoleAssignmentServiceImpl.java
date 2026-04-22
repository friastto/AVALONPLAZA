package org.frias.avalon.domain.roleassignment.application.service;

import jakarta.persistence.EntityNotFoundException;
import org.frias.avalon.domain.masterdata.services.interfaces.MasterDataService;
import org.frias.avalon.domain.roleassignment.domain.entity.RoleAssignment;
import org.frias.avalon.domain.roleassignment.infraestructure.RoleAssignmentRepository;
import org.frias.avalon.domain.user.domain.services.interfaces.UsersService;

public class RoleAssignmentServiceImpl implements RoleAssignmentService{
    private final RoleAssignmentRepository roleAssignmentRepository;
    private final UsersService usersService;
    private final MasterDataService masterDataService;
    private final ScheduleService scheduleService;

    public RoleAssignmentServiceImpl(RoleAssignmentRepository roleAssignmentRepository, UsersService usersService, MasterDataService masterDataService, ScheduleService scheduleService) {
        this.roleAssignmentRepository = roleAssignmentRepository;
        this.usersService = usersService;
        this.masterDataService = masterDataService;
        this.scheduleService = scheduleService;
    }

    @Override
    public RoleAssignment searchById(Long id) {

        return roleAssignmentRepository.findById(id).orElseThrow(()-> new EntityNotFoundException("no se encotro rol asignado"));
    }

    @Override
    public RoleAssignment save(
            Long userId,
            Long roleId,
            Long staffScopeId,
            Long scopeId,
            Long scheduleId,
            Long statusId
    ) {

        RoleAssignment roleAssignment = new RoleAssignment();
        roleAssignment.setUserAvalon( usersService.searchById(userId));
        roleAssignment.setRole( masterDataService.searchById(roleId));
        roleAssignment.setStaffScope(masterDataService.searchById(staffScopeId));
        roleAssignment.setScope(scopeId);
        roleAssignment.setSchedule(scheduleService.searchById(scheduleId));
        roleAssignment.setStatus( masterDataService.searchById(statusId));


        return roleAssignmentRepository.save(roleAssignment);

    }


}

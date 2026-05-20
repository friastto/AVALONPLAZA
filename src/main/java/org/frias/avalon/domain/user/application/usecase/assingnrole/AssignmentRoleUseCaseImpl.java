package org.frias.avalon.domain.user.application.usecase.assingnrole;

import jakarta.persistence.EntityNotFoundException;
import org.frias.avalon.core.exeptions.BusinessException;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.repository.MasterDataRepositoryPort;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.outlet.domain.model.OutletDomain;
import org.frias.avalon.domain.outlet.domain.port.OutletRepositoryPort;
import org.frias.avalon.domain.user.application.dtos.request.AssignmentRoleRequestDto;
import org.frias.avalon.domain.user.application.dtos.response.AssignmentRoleResponse;
import org.frias.avalon.domain.user.application.service.PermissionService;
import org.frias.avalon.domain.user.domain.mapper.RoleAssignmentMapper;
import org.frias.avalon.domain.user.domain.model.RoleAssignmentDomain;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.frias.avalon.domain.user.domain.port.RoleAssignmentRepositoryPort;
import org.frias.avalon.domain.user.domain.port.UserAvalonRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AssignmentRoleUseCaseImpl implements AssignmentRoleUseCase{

    private final UserAvalonRepositoryPort userPort;
    private final RoleAssignmentRepositoryPort rolePort;
    private final MasterDataRepositoryPort masterPort;
    private final RoleAssignmentMapper mapper;
    private final MasterTreeProvider treeProvider;
    private final OutletRepositoryPort outletRepositoryPort;
    private final PermissionService permissionService;


    public AssignmentRoleUseCaseImpl(UserAvalonRepositoryPort userPort, RoleAssignmentRepositoryPort rolePort, MasterDataRepositoryPort masterPort, RoleAssignmentMapper mapper, MasterTreeProvider treeProvider, OutletRepositoryPort outletRepositoryPort, PermissionService permissionService) {
        this.userPort = userPort;
        this.rolePort = rolePort;
        this.masterPort = masterPort;
        this.mapper = mapper;
        this.treeProvider = treeProvider;
        this.outletRepositoryPort = outletRepositoryPort;
        this.permissionService = permissionService;
    }

    @Transactional
    @Override
    public AssignmentRoleResponse execute(AssignmentRoleRequestDto request) {

        var tree = treeProvider.getTree();

        UserAvalonDomain user = userPort.findById(request.userId())
                    .orElseThrow(()-> new EntityNotFoundException("no se pudo encontrar el usuario"));

        MasterRoot userStatus = masterPort.findById(user.getStatusId())
                .orElseThrow(()-> new EntityNotFoundException("no se pudo validar el estado del usuario"));

        MasterRoot role = masterPort.findById(request.roleId())
                .orElseThrow(()-> new EntityNotFoundException("no se pudo encontrar el rol para asignar"));


        // 1. Validar que es rol
        if (!tree.isChildOf(role, "ROL")) { throw new RuntimeException("No es un rol válido"); }


        List<RoleAssignmentDomain> roleAssigned = rolePort.findByUserAvalonId(user.getId());

        RoleAssignmentDomain consumerRole = roleAssigned.stream()
                .filter(assignment ->
                        assignment.getOutletId() != null
                                && tree.isChildOf(
                                tree.getById(assignment.getRoleId()),
                                "EMP")
                                && tree.is(tree.getById(assignment.getStatus()),"ACT")

                )
                .findFirst()
                .orElse(null);

        MasterRoot currentRoleStatus = tree.getById(consumerRole.getStatus());


        //se regunta si tiene el rol de empleado activo si no lanza la exepcion para qeu el suaurio lo active
        if(!currentRoleStatus.isActive("ACT"))throw new IllegalStateException("este usuario tiene un rol de empleado desactivado");

        OutletDomain outletScope;

        // 2. Reglas de negocio
        if (tree.isChildOf(role, "CONS") && request.outletId() != null) {
            throw new RuntimeException("Cliente no se debe asignar a una tienda");

        }
        MasterRoot statusActive = masterPort.getActiveStatus()
                .orElseThrow(()-> new EntityNotFoundException("no se pudo activar el rol al usuario"));

        if(permissionService.canAssignRole(role,request.outletId())){

            outletScope = outletRepositoryPort.findById(request.outletId())
                    .orElseThrow(()-> new EntityNotFoundException("no se encontro la tienda. no se puede asignar el rol a esta tienda"));


            if (tree.isChildOf(role, "OPT") && request.outletId() == null) {
                throw new RuntimeException("Rol operativo requiere scope");
            }

            if(!outletScope.isActive(tree.getById(outletScope.getStatusId()).getShortName())){
                throw new BusinessException("la tienda no esta activa, no se pude asignar un usario con el rol -> "+role.getFullName());
            }



            rolePort.create(RoleAssignmentDomain.create(
                            user.getId(),role.getId(), outletScope.getId(), statusActive.getId()
                    )
            );

        }else{
            throw new BusinessException("no tiene los permisos para asignar el rol");
        }



        return  mapper.toResponse(user,userStatus, role, statusActive,outletScope.getId());
    }
}

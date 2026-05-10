package org.frias.avalon.domain.user.application.service;

import org.frias.avalon.domain.masterdata.application.dto.response.MasterDataResponseDto;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.outlet.application.dto.response.OutletInfoDto;
import org.frias.avalon.domain.outlet.application.dto.response.OutletResponseDto;
import org.frias.avalon.domain.outlet.domain.model.OutletDomain;
import org.frias.avalon.domain.outlet.domain.port.OutletRepositoryPort;
import org.frias.avalon.domain.user.application.dtos.response.modes.ClientModeDto;
import org.frias.avalon.domain.user.application.dtos.response.modes.EmployeeModeDto;
import org.frias.avalon.domain.user.application.dtos.response.modes.ModesResponseDto;
import org.frias.avalon.domain.user.application.dtos.results.ClientResult;
import org.frias.avalon.domain.user.application.dtos.results.EmployeeResult;
import org.frias.avalon.domain.user.application.dtos.results.ModesResult;
import org.frias.avalon.domain.user.domain.model.RoleAssignmentDomain;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class ModesMachineImpl implements ModesMachine{



    private final MasterTreeProvider treeProvider;
    private final PermissionService permissionService;
    private final OutletRepositoryPort outletPort;

    public ModesMachineImpl(MasterTreeProvider treeProvider, PermissionService permissionService, OutletRepositoryPort outletPort) {
        this.treeProvider = treeProvider;
        this.permissionService = permissionService;
        this.outletPort = outletPort;
    }


    @Override
    public ModesResult resolve(List<RoleAssignmentDomain> byUser, OutletDomain outletDomain) {


        MasterTree tree = treeProvider.getTree();

        ClientResult clientResult = null;
        EmployeeResult employeeResult = null;

        for(RoleAssignmentDomain roleAsigned : byUser){

            MasterRoot role  = tree.getById(roleAsigned.getRoleId());

            if (tree.isChildOf(role,"CONS")){

               List<String> permissionClient  = permissionService.resolvePermissions(role);

                clientResult = new ClientResult(
                        role.getFullName(),
                        true,
                        permissionClient
                );

            } else if (tree.isChildOf(role, "EMP")) {




                List<String> permissionEmployee  = permissionService.resolvePermissions(role);
                employeeResult = new EmployeeResult(
                        true,
                        new OutletInfoDto(outletDomain.getId(),outletDomain.getName()),
                        role,
                        permissionEmployee
                );
            }

        }


        return new ModesResult(clientResult,employeeResult);
    }

    @Override
    public ModesResponseDto mapperToResponse(ModesResult mode) {
        ClientModeDto client = null;
        EmployeeModeDto employee = null;
        if(mode.client() != null) {
            client = new ClientModeDto(
                    mode.client().type(),
                    mode.client().status(),
                    mode.client().permissions()
            );
        }
if (mode.employee() !=null) {
    OutletInfoDto outletDto = mode.employee().outlet();

    MasterDataResponseDto role = new MasterDataResponseDto(
            mode.employee().role().getId(),
            mode.employee().role().getShortName(),
            mode.employee().role().getFullName()
    );

    employee = new EmployeeModeDto(
            mode.employee().status(),
            outletDto,
            role,
            mode.employee().permissions()
    );
}

        return new ModesResponseDto(
                client,
               employee
        );
    }
}

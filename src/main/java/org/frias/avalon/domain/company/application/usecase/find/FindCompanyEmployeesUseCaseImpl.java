package org.frias.avalon.domain.company.application.usecase.find;

import org.frias.avalon.domain.company.application.dto.response.CompanyEmployeeResponse;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.outlet.domain.model.OutletDomain;
import org.frias.avalon.domain.outlet.domain.port.OutletRepositoryPort;
import org.frias.avalon.domain.person.domain.model.PersonDomain;
import org.frias.avalon.domain.person.domain.port.PersonRepositoryPort;
import org.frias.avalon.domain.user.domain.model.RoleAssignmentDomain;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.frias.avalon.domain.user.domain.port.RoleAssignmentRepositoryPort;
import org.frias.avalon.domain.user.domain.port.UserAvalonRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FindCompanyEmployeesUseCaseImpl implements FindCompanyEmployeesUseCase {

    private final RoleAssignmentRepositoryPort roleAssignmentRepositoryPort;
    private final OutletRepositoryPort outletRepositoryPort;
    private final UserAvalonRepositoryPort userAvalonRepositoryPort;
    private final PersonRepositoryPort personRepositoryPort;
    private final MasterTreeProvider masterTreeProvider;

    public FindCompanyEmployeesUseCaseImpl(
            RoleAssignmentRepositoryPort roleAssignmentRepositoryPort,
            OutletRepositoryPort outletRepositoryPort,
            UserAvalonRepositoryPort userAvalonRepositoryPort,
            PersonRepositoryPort personRepositoryPort,
            MasterTreeProvider masterTreeProvider
    ) {
        this.roleAssignmentRepositoryPort = roleAssignmentRepositoryPort;
        this.outletRepositoryPort = outletRepositoryPort;
        this.userAvalonRepositoryPort = userAvalonRepositoryPort;
        this.personRepositoryPort = personRepositoryPort;
        this.masterTreeProvider = masterTreeProvider;
    }

    @Override
    public List<CompanyEmployeeResponse> execute(Long companyId) {
        List<CompanyEmployeeResponse> result = new ArrayList<>();
        MasterTree tree = masterTreeProvider.getTree();

        // 1. Map all company outlets
        List<OutletDomain> outlets = outletRepositoryPort.findByCompanyId(companyId);
        Map<Long, String> outletNameMap = new HashMap<>();
        for (OutletDomain outlet : outlets) {
            outletNameMap.put(outlet.getId(), outlet.getName());
        }

        // 2. Gather all role assignments: Direct Company assignments + Outlet assignments
        List<RoleAssignmentDomain> allAssignments = new ArrayList<>(roleAssignmentRepositoryPort.findByCompanyId(companyId));
        for (OutletDomain outlet : outlets) {
            allAssignments.addAll(roleAssignmentRepositoryPort.findByOutletId(outlet.getId()));
        }

        Set<Long> processedAssignmentIds = new HashSet<>();

        for (RoleAssignmentDomain assignment : allAssignments) {
            if (assignment.getId() != null && !processedAssignmentIds.add(assignment.getId())) {
                continue;
            }

            Optional<UserAvalonDomain> userOpt = userAvalonRepositoryPort.findById(assignment.getUserId());
            if (userOpt.isEmpty()) {
                continue;
            }
            UserAvalonDomain user = userOpt.get();

            PersonDomain person = null;
            if (user.getPersonId() != null) {
                person = personRepositoryPort.findById(user.getPersonId()).orElse(null);
            }

            MasterRoot roleRoot = tree.getById(assignment.getRoleId());
            MasterRoot statusRoot = tree.getById(assignment.getStatus());

            String roleCode = roleRoot != null ? roleRoot.getShortName() : "";
            String roleName = roleRoot != null ? roleRoot.getFullName() : "";

            String category = "OPERATIVO";
            if (roleRoot != null && roleRoot.getParentId() != null) {
                MasterRoot parent = tree.getById(roleRoot.getParentId());
                if (parent != null) {
                    category = parent.getShortName();
                }
            }

            String outletName = "Sede Corporativa / Empresa";
            if (assignment.getOutletId() != null) {
                outletName = outletNameMap.getOrDefault(assignment.getOutletId(), "Tienda #" + assignment.getOutletId());
            }

            String statusName = statusRoot != null ? statusRoot.getFullName() : "ACTIVO";

            result.add(new CompanyEmployeeResponse(
                    user.getId(),
                    user.getUserName(),
                    person != null ? person.getId() : null,
                    person != null ? person.getName() : "SIN NOMBRE",
                    person != null ? person.getLastName() : "SIN APELLIDO",
                    person != null ? person.getNumberid() : "",
                    person != null ? person.getEmail() : "",
                    person != null ? person.getPhoneNumber() : null,
                    assignment.getRoleId(),
                    roleCode,
                    roleName,
                    category,
                    assignment.getOutletId(),
                    outletName,
                    assignment.getStatus(),
                    statusName
            ));
        }

        return result;
    }
}

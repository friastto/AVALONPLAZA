package org.frias.avalon.domain.company.application.usecase.find;

import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.domain.company.application.dto.response.CompanyServiceRequestDetailResponse;
import org.frias.avalon.domain.company.domain.model.CompanyDomain;
import org.frias.avalon.domain.company.domain.port.CompanyRepositoryPort;
import org.frias.avalon.domain.masterdata.application.dto.response.MasterRefDto;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class FindCompanyServiceRequestDetailUseCaseImpl implements FindCompanyServiceRequestDetailUseCase {

    private final CompanyRepositoryPort companyRepositoryPort;
    private final OutletRepositoryPort outletRepositoryPort;
    private final RoleAssignmentRepositoryPort roleAssignmentRepositoryPort;
    private final UserAvalonRepositoryPort userRepositoryPort;
    private final PersonRepositoryPort personRepositoryPort;
    private final MasterTreeProvider masterTreeProvider;

    public FindCompanyServiceRequestDetailUseCaseImpl(
            CompanyRepositoryPort companyRepositoryPort,
            OutletRepositoryPort outletRepositoryPort,
            RoleAssignmentRepositoryPort roleAssignmentRepositoryPort,
            UserAvalonRepositoryPort userRepositoryPort,
            PersonRepositoryPort personRepositoryPort,
            MasterTreeProvider masterTreeProvider
    ) {
        this.companyRepositoryPort = companyRepositoryPort;
        this.outletRepositoryPort = outletRepositoryPort;
        this.roleAssignmentRepositoryPort = roleAssignmentRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
        this.personRepositoryPort = personRepositoryPort;
        this.masterTreeProvider = masterTreeProvider;
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyServiceRequestDetailResponse execute(Long companyId) {
        CompanyDomain company = companyRepositoryPort.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada con id: " + companyId));

        MasterTree tree = masterTreeProvider.getTree();
        MasterRefDto statusRef = MasterRefDto.from(tree.getById(company.statusId()));

        List<OutletDomain> outlets = outletRepositoryPort.findByCompanyId(companyId);
        OutletDomain initialOutlet = outlets.isEmpty() ? null : outlets.get(0);

        Long gergenRoleId = tree.getByCodeOrThrow("GERGEN").getId();
        Optional<RoleAssignmentDomain> assignmentOpt = roleAssignmentRepositoryPort.findByCompanyIdAndRoleId(companyId, gergenRoleId);

        Long applicantUserId = null;
        String applicantFullName = "";
        String applicantIdentification = "";
        String applicantEmail = "";
        String applicantPhone = "";

        if (assignmentOpt.isPresent()) {
            applicantUserId = assignmentOpt.get().getUserId();
            Optional<UserAvalonDomain> userOpt = userRepositoryPort.findById(applicantUserId);
            if (userOpt.isPresent()) {
                Optional<PersonDomain> personOpt = personRepositoryPort.findById(userOpt.get().getPersonId());
                if (personOpt.isPresent()) {
                    PersonDomain person = personOpt.get();
                    applicantFullName = (person.getName() + " " + (person.getLastName() != null ? person.getLastName() : "")).trim();
                    applicantIdentification = person.getNumberid() != null ? person.getNumberid() : "";
                    applicantEmail = person.getEmail() != null ? person.getEmail() : "";
                    applicantPhone = person.getPhoneNumber() != null ? String.valueOf(person.getPhoneNumber()) : "";
                }
            }
        }

        Long outletId = (initialOutlet != null) ? initialOutlet.getId() : null;
        String storeName = (initialOutlet != null) ? initialOutlet.getName() : "";
        String storeAddress = (initialOutlet != null) ? initialOutlet.getAddress() : "";
        String storePhone = (initialOutlet != null) ? initialOutlet.getPhone() : "";
        Double latitude = (initialOutlet != null && initialOutlet.getLocation() != null) ? initialOutlet.getLocation().latitude() : null;
        Double longitude = (initialOutlet != null && initialOutlet.getLocation() != null) ? initialOutlet.getLocation().longitude() : null;

        return new CompanyServiceRequestDetailResponse(
                company.id(),
                company.name(),
                company.nit(),
                company.email(),
                company.statusId(),
                statusRef,
                company.createdAt(),
                outletId,
                storeName,
                storeAddress,
                storePhone,
                latitude,
                longitude,
                applicantUserId,
                applicantFullName,
                applicantIdentification,
                applicantEmail,
                applicantPhone
        );
    }
}

package org.frias.avalon.domain.company.application.usecase.create;

import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.domain.company.application.dto.request.CreateCompanyServiceRequestDto;
import org.frias.avalon.domain.company.application.dto.response.CompanyResponse;
import org.frias.avalon.domain.company.domain.model.CompanyDomain;
import org.frias.avalon.domain.company.domain.port.CompanyRepositoryPort;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.outlet.domain.model.LocationDomain;
import org.frias.avalon.domain.outlet.domain.model.OutletDomain;
import org.frias.avalon.domain.outlet.domain.port.OutletRepositoryPort;
import org.frias.avalon.domain.user.domain.model.RoleAssignmentDomain;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.frias.avalon.domain.user.domain.port.RoleAssignmentRepositoryPort;
import org.frias.avalon.domain.user.domain.port.UserAvalonRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class CreateCompanyServiceRequestUseCaseImpl implements CreateCompanyServiceRequestUseCase {

    private final CompanyRepositoryPort companyPort;
    private final OutletRepositoryPort outletPort;
    private final UserAvalonRepositoryPort userRepository;
    private final RoleAssignmentRepositoryPort roleAssignmentRepository;
    private final MasterTreeProvider masterTreeProvider;

    public CreateCompanyServiceRequestUseCaseImpl(
            CompanyRepositoryPort companyPort,
            OutletRepositoryPort outletPort,
            UserAvalonRepositoryPort userRepository,
            RoleAssignmentRepositoryPort roleAssignmentRepository,
            MasterTreeProvider masterTreeProvider
    ) {
        this.companyPort = companyPort;
        this.outletPort = outletPort;
        this.userRepository = userRepository;
        this.roleAssignmentRepository = roleAssignmentRepository;
        this.masterTreeProvider = masterTreeProvider;
    }

    @Override
    @Transactional
    public CompanyResponse execute(CreateCompanyServiceRequestDto request) {
        // 1. Validar unicidad del NIT
        companyPort.findByNit(request.companyNit()).ifPresent(existing -> {
            throw new IllegalStateException("Company with NIT " + request.companyNit() + " already exists");
        });

        // 2. Validar que el usuario solicitante exista
        UserAvalonDomain applicant = userRepository.findById(request.applicantUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario solicitante no encontrado con id: " + request.applicantUserId()));

        MasterTree tree = masterTreeProvider.getTree();

        // 3. Resolver estado inicial de la empresa (RVW / revision)
        MasterRoot rvwNode = tree.getByCode("RVW");
        Long companyStatusId = (rvwNode != null) ? rvwNode.getId() : tree.getByCodeOrThrow("ACT").getId();

        // 4. Resolver estado inactivo/pendiente para outlet y rol
        MasterRoot inaNode = tree.getByCode("INA");
        if (inaNode == null) {
            inaNode = tree.getByCode("INACT");
        }
        Long inactiveStatusId = (inaNode != null) ? inaNode.getId() : tree.getByCodeOrThrow("ACT").getId();

        // 5. Crear y guardar la empresa en public.company
        CompanyDomain toSave = new CompanyDomain(
                null,
                request.companyNit().trim(),
                request.companyName().trim(),
                request.companyEmail() != null ? request.companyEmail().trim() : null,
                companyStatusId,
                BigDecimal.ZERO,
                null,
                null
        );
        CompanyDomain savedCompany = companyPort.save(toSave);

        // 6. Crear la tienda inicial en public.outlet con coordenadas GPS
        Double lat = request.latitude() != null ? request.latitude() : 4.60971;
        Double lon = request.longitude() != null ? request.longitude() : -74.08175;
        LocationDomain location = new LocationDomain(lat, lon);

        String phone = (request.storePhone() != null && !request.storePhone().isBlank())
                ? request.storePhone().trim()
                : "0000000";

        OutletDomain outletDomain = OutletDomain.create(
                request.storeName().trim(),
                request.storeAddress().trim(),
                phone,
                request.companyNit().trim(),
                inactiveStatusId,
                location,
                BigDecimal.ZERO,
                savedCompany.id()
        );
        outletPort.save(outletDomain);

        // 7. Crear postulacion de rol GERGEN inactivo (se activara al aprobarse la empresa)
        MasterRoot gergenRole = tree.getByCodeOrThrow("GERGEN");
        RoleAssignmentDomain roleAssignment = RoleAssignmentDomain.createCompanyRole(
                applicant.getId(),
                gergenRole.getId(),
                savedCompany.id(),
                inactiveStatusId
        );
        roleAssignmentRepository.create(roleAssignment);

        return CompanyResponse.from(savedCompany, tree);
    }
}

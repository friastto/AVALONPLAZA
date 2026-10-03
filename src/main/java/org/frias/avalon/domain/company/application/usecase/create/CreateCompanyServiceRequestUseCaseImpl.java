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
import org.frias.avalon.domain.person.domain.model.PersonDomain;
import org.frias.avalon.domain.person.domain.port.PersonRepositoryPort;
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
    private final PersonRepositoryPort personPort;
    private final RoleAssignmentRepositoryPort roleAssignmentRepository;
    private final MasterTreeProvider masterTreeProvider;

    public CreateCompanyServiceRequestUseCaseImpl(
            CompanyRepositoryPort companyPort,
            OutletRepositoryPort outletPort,
            UserAvalonRepositoryPort userRepository,
            PersonRepositoryPort personPort,
            RoleAssignmentRepositoryPort roleAssignmentRepository,
            MasterTreeProvider masterTreeProvider
    ) {
        this.companyPort = companyPort;
        this.outletPort = outletPort;
        this.userRepository = userRepository;
        this.personPort = personPort;
        this.roleAssignmentRepository = roleAssignmentRepository;
        this.masterTreeProvider = masterTreeProvider;
    }

    @Override
    @Transactional
    public CompanyResponse execute(CreateCompanyServiceRequestDto request) {
        // 1. Validar que el usuario solicitante exista
        UserAvalonDomain applicant = userRepository.findById(request.applicantUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario solicitante no encontrado con id: " + request.applicantUserId()));

        PersonDomain applicantPerson = (applicant.getPersonId() != null)
                ? personPort.findById(applicant.getPersonId()).orElse(null)
                : null;

        // 2. Determinar tipo de solicitud (PERSONA_NATURAL vs EMPRESA)
        String requestType = (request.requestType() != null && !request.requestType().isBlank())
                ? request.requestType().trim().toUpperCase()
                : "EMPRESA";
        boolean isPersonaNatural = "PERSONA_NATURAL".equals(requestType);

        String finalCompanyNit;
        String finalCompanyName;
        String finalCompanyEmail;

        if (isPersonaNatural) {
            // Resolver NIT desde identificacion personal o campo provisto
            if (request.companyNit() != null && !request.companyNit().isBlank()) {
                finalCompanyNit = request.companyNit().trim();
            } else if (applicantPerson != null && applicantPerson.getNumberid() != null && !applicantPerson.getNumberid().isBlank()) {
                finalCompanyNit = applicantPerson.getNumberid().trim();
            } else {
                throw new IllegalArgumentException("El numero de identificacion personal es obligatorio para persona natural");
            }

            // Resolver nombre de la empresa / titular
            if (request.companyName() != null && !request.companyName().isBlank()) {
                finalCompanyName = request.companyName().trim();
            } else if (applicantPerson != null && applicantPerson.getName() != null) {
                String lastName = applicantPerson.getLastName() != null ? " " + applicantPerson.getLastName().trim() : "";
                finalCompanyName = applicantPerson.getName().trim() + lastName;
            } else {
                finalCompanyName = request.storeName().trim();
            }

            // Resolver email
            if (request.companyEmail() != null && !request.companyEmail().isBlank()) {
                finalCompanyEmail = request.companyEmail().trim();
            } else if (applicantPerson != null && applicantPerson.getEmail() != null) {
                finalCompanyEmail = applicantPerson.getEmail().trim();
            } else {
                finalCompanyEmail = null;
            }
        } else {
            // Para EMPRESA / Organizacion, NIT y Nombre son estrictamente requeridos
            if (request.companyNit() == null || request.companyNit().isBlank()) {
                throw new IllegalArgumentException("El NIT de la empresa es obligatorio");
            }
            if (request.companyName() == null || request.companyName().isBlank()) {
                throw new IllegalArgumentException("El nombre de la empresa es obligatorio");
            }
            finalCompanyNit = request.companyNit().trim();
            finalCompanyName = request.companyName().trim();
            finalCompanyEmail = (request.companyEmail() != null && !request.companyEmail().isBlank())
                    ? request.companyEmail().trim()
                    : null;
        }

        // 3. Validar unicidad del NIT de la empresa / titular
        companyPort.findByNit(finalCompanyNit).ifPresent(existing -> {
            throw new IllegalStateException("Ya existe una empresa o titular registrado con el NIT/Documento " + finalCompanyNit);
        });

        // 4. Validar y resolver el NIT de la tienda inicial (storeNit)
        String finalStoreNit = (request.storeNit() != null && !request.storeNit().isBlank())
                ? request.storeNit().trim()
                : finalCompanyNit;

        outletPort.findByNit(finalStoreNit).ifPresent(existing -> {
            throw new IllegalStateException("Ya existe una tienda registrada con el NIT " + finalStoreNit);
        });

        MasterTree tree = masterTreeProvider.getTree();

        // 5. Resolver estado inicial de la empresa (RVW / revision)
        MasterRoot rvwNode = tree.getByCode("RVW");
        Long companyStatusId = (rvwNode != null) ? rvwNode.getId() : tree.getByCodeOrThrow("ACT").getId();

        // 6. Resolver estado inactivo/pendiente para outlet y rol
        MasterRoot inaNode = tree.getByCode("INA");
        if (inaNode == null) {
            inaNode = tree.getByCode("INACT");
        }
        Long inactiveStatusId = (inaNode != null) ? inaNode.getId() : tree.getByCodeOrThrow("ACT").getId();

        // 7. Crear y guardar la entidad Company
        CompanyDomain toSave = new CompanyDomain(
                null,
                finalCompanyNit,
                finalCompanyName,
                finalCompanyEmail,
                companyStatusId,
                BigDecimal.ZERO,
                null,
                null
        );
        CompanyDomain savedCompany = companyPort.save(toSave);

        // 8. Crear la tienda inicial en public.outlet con su storeNit propio y coordenadas GPS
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
                finalStoreNit,
                inactiveStatusId,
                location,
                BigDecimal.ZERO,
                savedCompany.id()
        );
        outletPort.save(outletDomain);

        // 9. Crear postulacion de rol GERGEN inactivo (se activara al aprobarse la empresa)
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


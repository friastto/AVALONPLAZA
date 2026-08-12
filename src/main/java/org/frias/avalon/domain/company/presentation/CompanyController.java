package org.frias.avalon.domain.company.presentation;

import jakarta.validation.Valid;
import org.frias.avalon.core.exeptions.ApiResponse;
import org.frias.avalon.domain.company.application.dto.request.CreateCompanyRequest;
import org.frias.avalon.domain.company.application.dto.response.CompanyResponse;
import org.frias.avalon.domain.company.application.usecase.create.CreateCompanyUseCase;
import org.frias.avalon.domain.company.application.usecase.find.FindAllCompaniesUseCase;
import org.frias.avalon.domain.company.domain.port.CompanyRepositoryPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing companies.
 * Exposes endpoints /api/v1/companies for GET, POST, and PUT operations.
 */
@RestController
@RequestMapping("/api/v1/companies")
public class CompanyController {

    private final CreateCompanyUseCase createCompanyUseCase;
    private final FindAllCompaniesUseCase findAllCompaniesUseCase;
    private final CompanyRepositoryPort companyRepositoryPort;
    private final org.frias.avalon.domain.outlet.domain.port.OutletRepositoryPort outletRepositoryPort;
    private final org.frias.avalon.domain.outlet.infraestructure.mapper.OutletMapper outletMapper;

    public CompanyController(
            CreateCompanyUseCase createCompanyUseCase,
            FindAllCompaniesUseCase findAllCompaniesUseCase,
            CompanyRepositoryPort companyRepositoryPort,
            org.frias.avalon.domain.outlet.domain.port.OutletRepositoryPort outletRepositoryPort,
            org.frias.avalon.domain.outlet.infraestructure.mapper.OutletMapper outletMapper
    ) {
        this.createCompanyUseCase = createCompanyUseCase;
        this.findAllCompaniesUseCase = findAllCompaniesUseCase;
        this.companyRepositoryPort = companyRepositoryPort;
        this.outletRepositoryPort = outletRepositoryPort;
        this.outletMapper = outletMapper;
    }

    /**
     * GET /api/v1/companies - Retrieves all companies.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CompanyResponse>>> findAll() {
        List<CompanyResponse> companies = findAllCompaniesUseCase.execute();
        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.value(),
                companies.isEmpty() ? "No companies found" : "Companies retrieved successfully",
                companies
        ));
    }

    /**
     * GET /api/v1/companies/{id} - Retrieves company details by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CompanyResponse>> findById(@PathVariable Long id) {
        return companyRepositoryPort.findById(id)
                .map(company -> ResponseEntity.ok(new ApiResponse<>(
                        HttpStatus.OK.value(),
                        "Company retrieved successfully",
                        new CompanyResponse(
                                company.id(),
                                company.nit(),
                                company.name(),
                                company.email(),
                                company.statusId(),
                                company.defaultCashThresholdAmount(),
                                company.createdAt(),
                                company.updatedAt()
                        )
                )))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(HttpStatus.NOT_FOUND.value(), "Company not found", null)));
    }

    /**
     * GET /api/v1/companies/{id}/outlets - Retrieves all outlets linked to company.
     */
    @GetMapping("/{id}/outlets")
    public ResponseEntity<ApiResponse<List<org.frias.avalon.domain.outlet.application.dto.response.OutletResponseDto>>> findOutletsByCompany(@PathVariable Long id) {
        List<org.frias.avalon.domain.outlet.domain.model.OutletDomain> outlets = outletRepositoryPort.findByCompanyId(id);
        List<org.frias.avalon.domain.outlet.application.dto.response.OutletResponseDto> responseDtos = outlets.stream()
                .map(outletMapper::toResponse)
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.value(),
                responseDtos.isEmpty() ? "No outlets found for company" : "Outlets retrieved successfully",
                responseDtos
        ));
    }

    /**
     * POST /api/v1/companies - Creates a new company.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CompanyResponse>> create(@Valid @RequestBody CreateCompanyRequest request) {
        CompanyResponse createdCompany = createCompanyUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                        HttpStatus.CREATED.value(),
                        "Company created successfully",
                        createdCompany
                ));
    }

    /**
     * PUT /api/v1/companies/{id}/threshold - Updates default cash drop threshold for company.
     */
    @PutMapping("/{id}/threshold")
    public ResponseEntity<ApiResponse<Void>> updateThreshold(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, java.math.BigDecimal> payload
    ) {
        java.math.BigDecimal thresholdAmount = payload.get("thresholdAmount");
        companyRepositoryPort.updateDefaultThreshold(id, thresholdAmount);
        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.value(),
                "Default company threshold updated successfully",
                null
        ));
    }
}

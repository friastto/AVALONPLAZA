package org.frias.avalon.domain.company.presentation;

import jakarta.validation.Valid;
import org.frias.avalon.core.exeptions.ApiResponse;
import org.frias.avalon.domain.company.application.dto.request.CreateCompanyRequest;
import org.frias.avalon.domain.company.application.dto.response.CompanyResponse;
import org.frias.avalon.domain.company.application.usecase.approve.ApproveCompanyUseCase;
import org.frias.avalon.domain.company.application.usecase.create.CreateCompanyUseCase;
import org.frias.avalon.domain.company.application.usecase.find.FindAllCompaniesUseCase;
import org.frias.avalon.domain.company.application.usecase.find.FindCompanyByIdUseCase;
import org.frias.avalon.domain.company.application.usecase.find.FindOutletsByCompanyUseCase;
import org.frias.avalon.domain.company.application.usecase.find.FindPendingCompaniesUseCase;
import org.frias.avalon.domain.outlet.application.dto.response.OutletResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Pure Clean Architecture REST Controller for managing companies.
 * Strictly orchestrates application Use Cases and returns standardized ApiResponse wrappers.
 */
@RestController
@RequestMapping("/api/v1/companies")
public class CompanyController {

    private final CreateCompanyUseCase createCompanyUseCase;
    private final FindAllCompaniesUseCase findAllCompaniesUseCase;
    private final FindPendingCompaniesUseCase findPendingCompaniesUseCase;
    private final FindCompanyByIdUseCase findCompanyByIdUseCase;
    private final FindOutletsByCompanyUseCase findOutletsByCompanyUseCase;
    private final ApproveCompanyUseCase approveCompanyUseCase;

    public CompanyController(
            CreateCompanyUseCase createCompanyUseCase,
            FindAllCompaniesUseCase findAllCompaniesUseCase,
            FindPendingCompaniesUseCase findPendingCompaniesUseCase,
            FindCompanyByIdUseCase findCompanyByIdUseCase,
            FindOutletsByCompanyUseCase findOutletsByCompanyUseCase,
            ApproveCompanyUseCase approveCompanyUseCase
    ) {
        this.createCompanyUseCase = createCompanyUseCase;
        this.findAllCompaniesUseCase = findAllCompaniesUseCase;
        this.findPendingCompaniesUseCase = findPendingCompaniesUseCase;
        this.findCompanyByIdUseCase = findCompanyByIdUseCase;
        this.findOutletsByCompanyUseCase = findOutletsByCompanyUseCase;
        this.approveCompanyUseCase = approveCompanyUseCase;
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
     * GET /api/v1/companies/pending - Retrieves companies pending approval (status RVW).
     */
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<CompanyResponse>>> findPending() {
        List<CompanyResponse> pendingCompanies = findPendingCompaniesUseCase.execute();
        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.value(),
                pendingCompanies.isEmpty() ? "No pending companies found" : "Pending companies retrieved successfully",
                pendingCompanies
        ));
    }

    /**
     * GET /api/v1/companies/{id} - Retrieves company details by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CompanyResponse>> findById(@PathVariable Long id) {
        return findCompanyByIdUseCase.execute(id)
                .map(company -> ResponseEntity.ok(new ApiResponse<>(
                        HttpStatus.OK.value(),
                        "Company retrieved successfully",
                        company
                )))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(HttpStatus.NOT_FOUND.value(), "Company not found", null)));
    }

    /**
     * GET /api/v1/companies/{id}/outlets - Retrieves all outlets linked to company.
     */
    @GetMapping("/{id}/outlets")
    public ResponseEntity<ApiResponse<List<OutletResponseDto>>> findOutletsByCompany(@PathVariable Long id) {
        List<OutletResponseDto> responseDtos = findOutletsByCompanyUseCase.execute(id);
        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.value(),
                responseDtos.isEmpty() ? "No outlets found for company" : "Outlets retrieved successfully",
                responseDtos
        ));
    }

    /**
     * POST /api/v1/companies - Creates a new company request.
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
     * POST /api/v1/companies/{id}/approve - Approves company request and provisions tenant schema.
     */
    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<CompanyResponse>> approveCompany(@PathVariable Long id) {
        CompanyResponse approved = approveCompanyUseCase.execute(id);
        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.value(),
                "Company approved successfully and tenant schema provisioned",
                approved
        ));
    }
}

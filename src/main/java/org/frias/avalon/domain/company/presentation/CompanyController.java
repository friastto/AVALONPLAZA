package org.frias.avalon.domain.company.presentation;

import jakarta.validation.Valid;
import org.frias.avalon.core.exeptions.ApiResponse;
import org.frias.avalon.domain.company.application.dto.request.CreateCompanyRequest;
import org.frias.avalon.domain.company.application.dto.response.CompanyDashboardResponse;
import org.frias.avalon.domain.company.application.dto.response.CompanyResponse;
import org.frias.avalon.domain.company.application.dto.request.AssignCompanyManagerRequest;
import org.frias.avalon.domain.company.application.dto.response.CompanyManagerResponse;
import org.frias.avalon.domain.company.application.dto.request.AssignCompanyEmployeeRequest;
import org.frias.avalon.domain.company.application.dto.response.CompanyEmployeeResponse;
import org.frias.avalon.domain.company.application.usecase.approve.ApproveCompanyUseCase;
import org.frias.avalon.domain.company.application.usecase.assign.AssignCompanyEmployeeUseCase;
import org.frias.avalon.domain.company.application.usecase.assign.AssignCompanyManagerUseCase;
import org.frias.avalon.domain.company.application.usecase.create.CreateCompanyUseCase;
import org.frias.avalon.domain.company.application.usecase.find.*;
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
    private final GetCompanyDashboardUseCase getCompanyDashboardUseCase;
    private final AssignCompanyManagerUseCase assignCompanyManagerUseCase;
    private final GetCompanyManagerUseCase getCompanyManagerUseCase;
    private final FindCompanyEmployeesUseCase findCompanyEmployeesUseCase;
    private final AssignCompanyEmployeeUseCase assignCompanyEmployeeUseCase;

    public CompanyController(
            CreateCompanyUseCase createCompanyUseCase,
            FindAllCompaniesUseCase findAllCompaniesUseCase,
            FindPendingCompaniesUseCase findPendingCompaniesUseCase,
            FindCompanyByIdUseCase findCompanyByIdUseCase,
            FindOutletsByCompanyUseCase findOutletsByCompanyUseCase,
            ApproveCompanyUseCase approveCompanyUseCase,
            GetCompanyDashboardUseCase getCompanyDashboardUseCase,
            AssignCompanyManagerUseCase assignCompanyManagerUseCase,
            GetCompanyManagerUseCase getCompanyManagerUseCase,
            FindCompanyEmployeesUseCase findCompanyEmployeesUseCase,
            AssignCompanyEmployeeUseCase assignCompanyEmployeeUseCase
    ) {
        this.createCompanyUseCase = createCompanyUseCase;
        this.findAllCompaniesUseCase = findAllCompaniesUseCase;
        this.findPendingCompaniesUseCase = findPendingCompaniesUseCase;
        this.findCompanyByIdUseCase = findCompanyByIdUseCase;
        this.findOutletsByCompanyUseCase = findOutletsByCompanyUseCase;
        this.approveCompanyUseCase = approveCompanyUseCase;
        this.getCompanyDashboardUseCase = getCompanyDashboardUseCase;
        this.assignCompanyManagerUseCase = assignCompanyManagerUseCase;
        this.getCompanyManagerUseCase = getCompanyManagerUseCase;
        this.findCompanyEmployeesUseCase = findCompanyEmployeesUseCase;
        this.assignCompanyEmployeeUseCase = assignCompanyEmployeeUseCase;
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

    /**
     * GET /api/v1/companies/{id}/dashboard - Retrieves company-wide consolidated financial dashboard.
     *
     * @param id        Company ID.
     * @param period    Temporal filter ('HOY', 'MES', 'ANIO', 'HISTORICO').
     * @param outletId  Optional outlet ID to filter down to a specific store.
     * @return Consolidated metrics and store performance.
     */
    @GetMapping("/{id}/dashboard")
    public ResponseEntity<ApiResponse<CompanyDashboardResponse>> getCompanyDashboard(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "MES") String period,
            @RequestParam(required = false) Long outletId
    ) {
        CompanyDashboardResponse dashboard = getCompanyDashboardUseCase.execute(id, period, outletId);
        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.value(),
                "Company dashboard retrieved successfully",
                dashboard
        ));
    }

    /**
     * POST /api/v1/companies/{id}/manager - Assigns or creates a company general manager (GERGEN).
     */
    @PostMapping("/{id}/manager")
    public ResponseEntity<ApiResponse<CompanyManagerResponse>> assignManager(
            @PathVariable Long id,
            @Valid @RequestBody AssignCompanyManagerRequest request
    ) {
        CompanyManagerResponse response = assignCompanyManagerUseCase.execute(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                        HttpStatus.CREATED.value(),
                        "Company manager assigned successfully",
                        response
                ));
    }

    /**
     * GET /api/v1/companies/{id}/manager - Retrieves the active general manager of the company.
     */
    @GetMapping("/{id}/manager")
    public ResponseEntity<ApiResponse<CompanyManagerResponse>> getManager(@PathVariable Long id) {
        CompanyManagerResponse response = getCompanyManagerUseCase.execute(id);
        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.value(),
                "Company manager retrieved successfully",
                response
        ));
    }

    /**
     * GET /api/v1/companies/{id}/employees - Retrieves all staff/employees for company and its outlets.
     */
    @GetMapping("/{id}/employees")
    public ResponseEntity<ApiResponse<List<CompanyEmployeeResponse>>> getEmployees(@PathVariable Long id) {
        List<CompanyEmployeeResponse> response = findCompanyEmployeesUseCase.execute(id);
        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.value(),
                response.isEmpty() ? "No employees found for company" : "Company employees retrieved successfully",
                response
        ));
    }

    /**
     * POST /api/v1/companies/{id}/employees - Assigns or creates a new employee for company or its outlets.
     */
    @PostMapping("/{id}/employees")
    public ResponseEntity<ApiResponse<CompanyEmployeeResponse>> assignEmployee(
            @PathVariable Long id,
            @Valid @RequestBody AssignCompanyEmployeeRequest request
    ) {
        CompanyEmployeeResponse response = assignCompanyEmployeeUseCase.execute(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                        HttpStatus.CREATED.value(),
                        "Company employee assigned successfully",
                        response
                ));
    }
}

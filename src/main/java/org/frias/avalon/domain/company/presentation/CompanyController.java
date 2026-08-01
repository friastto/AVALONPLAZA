package org.frias.avalon.domain.company.presentation;

import jakarta.validation.Valid;
import org.frias.avalon.core.exeptions.ApiResponse;
import org.frias.avalon.domain.company.application.dto.request.CreateCompanyRequest;
import org.frias.avalon.domain.company.application.dto.response.CompanyResponse;
import org.frias.avalon.domain.company.application.usecase.create.CreateCompanyUseCase;
import org.frias.avalon.domain.company.application.usecase.find.FindAllCompaniesUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing companies.
 * Exposes endpoints /api/v1/companies for GET and POST operations.
 */
@RestController
@RequestMapping("/api/v1/companies")
public class CompanyController {

    private final CreateCompanyUseCase createCompanyUseCase;
    private final FindAllCompaniesUseCase findAllCompaniesUseCase;

    public CompanyController(CreateCompanyUseCase createCompanyUseCase, FindAllCompaniesUseCase findAllCompaniesUseCase) {
        this.createCompanyUseCase = createCompanyUseCase;
        this.findAllCompaniesUseCase = findAllCompaniesUseCase;
    }

    /**
     * GET /api/v1/companies - Retrieves all companies.
     *
     * @return ResponseEntity with list of companies
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
     * POST /api/v1/companies - Creates a new company.
     *
     * @param request CreateCompanyRequest DTO
     * @return ResponseEntity with created company
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
}

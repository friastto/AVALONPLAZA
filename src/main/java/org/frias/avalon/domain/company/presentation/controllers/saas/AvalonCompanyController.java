package org.frias.avalon.domain.company.presentation.controllers.saas;

import jakarta.validation.Valid;
import org.frias.avalon.core.exeptions.ApiResponse;
import org.frias.avalon.domain.company.application.dtos.request.CompanyWithOutletDto;
import org.frias.avalon.domain.company.application.dtos.response.CompanyWhithMainOutletResponseDto;
import org.frias.avalon.domain.company.application.usecase.inter.saas.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/avalon/admin/saas/company")
public class AvalonCompanyController {

    private final CreateCompanyWithMainOutletUseCase createCompanyWhitOutlets;
    private final DeleteCompanyByIdUseCase deleteCompanyUseCase;
    private final ChangeStatusCompanyUseCase changeStatusCompanyUseCase;
    private final SearchCompanyUseCase searchCompanyUseCase;
    private final GetAllCompanyUseCase getAllCompanyUseCase;

    public AvalonCompanyController(CreateCompanyWithMainOutletUseCase createCompanyWhitOutlets, DeleteCompanyByIdUseCase deleteCompanyUseCase, ChangeStatusCompanyUseCase changeStatusCompanyUseCase, SearchCompanyUseCase searchCompanyUseCase, GetAllCompanyUseCase getAllCompanyUseCase) {
        this.createCompanyWhitOutlets = createCompanyWhitOutlets;
        this.deleteCompanyUseCase = deleteCompanyUseCase;
        this.changeStatusCompanyUseCase = changeStatusCompanyUseCase;
        this.searchCompanyUseCase = searchCompanyUseCase;
        this.getAllCompanyUseCase = getAllCompanyUseCase;
    }


    @PostMapping("/create")
    public ResponseEntity<ApiResponse<CompanyWhithMainOutletResponseDto>> create(@Valid @RequestBody CompanyWithOutletDto companyWithOutletDto) {

        CompanyWhithMainOutletResponseDto companyWithOutlet = createCompanyWhitOutlets.execute(companyWithOutletDto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(201, "Empresa Creada Exitosamente", companyWithOutlet));
    }

    @PutMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<CompanyWhithMainOutletResponseDto>> delete(@PathVariable Long id) {

        deleteCompanyUseCase.execute(id);

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(404, "Empresa Creada Exitosamente", null));
    }

    @PutMapping("/changeStatus/{id}")
    public ResponseEntity<ApiResponse<CompanyWhithMainOutletResponseDto>> suspend(@PathVariable Long id, @RequestParam("idStatus") Long idStatus) {

        CompanyWhithMainOutletResponseDto companyWithMainOutlet = changeStatusCompanyUseCase.execute(id,idStatus);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>(200, "Se Estalecio el estado *"+companyWithMainOutlet.status()+"* exitosamente", companyWithMainOutlet));
    }

    @GetMapping("/search/v1/{id}")
    public ResponseEntity<ApiResponse<CompanyWhithMainOutletResponseDto>> suspend(@PathVariable Long id) {

        CompanyWhithMainOutletResponseDto companyWithMainOutlet = searchCompanyUseCase.execute(id);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>(200, "Se Estalecio el estado *"+companyWithMainOutlet.status()+"* exitosamente", companyWithMainOutlet));
    }


    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<CompanyWhithMainOutletResponseDto>>> getAll() {

        List<CompanyWhithMainOutletResponseDto> companyWithMainOutletList = getAllCompanyUseCase.execute();

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>(200, "Se extrageron las empresas afiliadas en avalon corretamente", companyWithMainOutletList));
    }


}

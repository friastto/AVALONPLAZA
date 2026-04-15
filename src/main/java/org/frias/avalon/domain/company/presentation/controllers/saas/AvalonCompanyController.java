package org.frias.avalon.domain.company.presentation.controllers.saas;

import jakarta.validation.Valid;
import org.frias.avalon.core.exeptions.ApiResponse;
import org.frias.avalon.domain.company.application.dtos.request.CompanyWithOutletDto;
import org.frias.avalon.domain.company.application.dtos.response.CompanyWhithMainOutletResponseDto;
import org.frias.avalon.domain.company.application.usecase.inter.saas.CreateCompanyWithMainOutletUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


public class AvalonCompanyController {

    private final CreateCompanyWithMainOutletUseCase createCompanyWhitOutlets;

    public AvalonCompanyController(CreateCompanyWithMainOutletUseCase createCompanyWhitOutlets) {
        this.createCompanyWhitOutlets = createCompanyWhitOutlets;
    }


    @PostMapping("/create")
    public ResponseEntity<ApiResponse<CompanyWhithMainOutletResponseDto>> create(@Valid @RequestBody CompanyWithOutletDto companyWithOutletDto) {

        CompanyWhithMainOutletResponseDto cwo = createCompanyWhitOutlets.execute(companyWithOutletDto);

        return  ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(201, "Empresa Creada Exitosamente", cwo));
    }


}

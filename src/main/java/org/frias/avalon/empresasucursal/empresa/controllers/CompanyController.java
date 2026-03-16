package org.frias.avalon.empresasucursal.empresa.controllers;



import jakarta.validation.Valid;
import org.frias.avalon.empresasucursal.empresa.dtos.CompanyResponseDto;
import org.frias.avalon.empresasucursal.empresa.dtos.CompanyRequestNewDto;
import org.frias.avalon.empresasucursal.empresa.services.interfaces.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/company")
public class CompanyController {

    @Autowired
    CompanyService companyService;


    @PostMapping("/save")
    public CompanyResponseDto save(@Valid @RequestBody CompanyRequestNewDto companyNewDto) {

        return companyService.save(companyNewDto);
    }

    @GetMapping("/outlets")
    public CompanyResponseDto searchOutlets() {

        return companyService.searchCompanyAndOutlets();
    }

}

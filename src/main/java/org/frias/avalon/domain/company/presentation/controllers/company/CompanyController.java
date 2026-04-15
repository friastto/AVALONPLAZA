package org.frias.avalon.domain.company.presentation.controllers.company;


import jakarta.validation.Valid;
import org.frias.avalon.domain.company.application.dtos.request.CompanyRequestNewDto;
import org.frias.avalon.domain.company.application.dtos.CompanyResponseDto;
import org.frias.avalon.domain.company.application.services.interfaces.CompanyService;
import org.frias.avalon.domain.inventory.Producto.modules.admincompany.services.interfaces.ProductoCompanyService;
import org.frias.avalon.domain.product.application.dto.company.ProductResponseDto;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/avalon/company")
public class CompanyController {

    private final CompanyService companyService;
    private final ProductoCompanyService productoCompanyService;

    public CompanyController(CompanyService companyService, ProductoCompanyService productoCompanyService) {
        this.companyService = companyService;
        this.productoCompanyService = productoCompanyService;
    }


    @PostMapping("/create")
    public CompanyResponseDto create(@Valid @RequestBody CompanyRequestNewDto companyNewDto) {

        return companyService.createCompanyWhitOutlets(companyNewDto);
    }

    @GetMapping("/outlets")
    public CompanyResponseDto searchOutlets() {

        return companyService.searchCompanyAndOutlets();
    }
    @PostMapping("/add-from-avalon/{avalonProductId}")
    public ProductResponseDto addProductToCatalog(@PathVariable Long avalonProductId) {

        return productoCompanyService.addSaasProductToCompanyCatalog(avalonProductId);
    }

}

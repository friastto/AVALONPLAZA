package org.frias.avalon.domain.company.A;



import jakarta.validation.Valid;
import org.frias.avalon.temp.inventory.Producto.modules.admincompany.services.interfaces.ProductoCompanyService;
import org.frias.avalon.temp.inventory.Producto.modules.adminsaas.dtos.ProductResponseDto;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/master/company")
public class CompanyController {

    private final CompanyService companyService;
    private final ProductoCompanyService productoCompanyService;

    public CompanyController(CompanyService companyService, ProductoCompanyService productoCompanyService) {
        this.companyService = companyService;
        this.productoCompanyService = productoCompanyService;
    }


    @PostMapping("/create")
    public CompanyResponseDto save(@Valid @RequestBody CompanyRequestNewDto companyNewDto) {

        return companyService.save(companyNewDto);
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

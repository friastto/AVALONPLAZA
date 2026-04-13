package org.frias.avalon.domain.inventory.Producto.modules.admincompany.controllers;

import org.frias.avalon.domain.inventory.Producto.modules.admincompany.services.interfaces.ProductoCompanyService;
import org.frias.avalon.domain.product.application.dto.company.ProductRequestCreate;
import org.frias.avalon.domain.product.application.dto.company.ProductResponseDto;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/productCompany")
public class ProductoCompanyController {

    private final ProductoCompanyService productoCompanyService;

    public ProductoCompanyController(ProductoCompanyService productoCompanyService) {
        this.productoCompanyService = productoCompanyService;
    }

    @PostMapping("/add/prodcutc/{id}/toCompany")
    public ProductResponseDto addToCatalogCompany(@PathVariable Long id) {
        return   productoCompanyService.addSaasProductToCompanyCatalog(id);

    }

    @GetMapping("/all")
    public List<ProductResponseDto> getCatalogCompany() {
        return   productoCompanyService.getAll();

    }
    @PutMapping(value = "/update/{id}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProductResponseDto getCatalogCompany(

            @PathVariable Long id,
            @RequestPart("data") ProductRequestCreate data,
            @RequestPart("image") MultipartFile img) {

        return   productoCompanyService.update(id, data,img);

    }
}

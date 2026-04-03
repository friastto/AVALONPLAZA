package org.frias.avalon.domain.inventory.Producto.modules.admincompany.controllers;

import jakarta.validation.Valid;
import org.frias.avalon.core.uploadimg.service.ProductUploadImgImpl;
import org.frias.avalon.domain.inventory.Producto.modules.admincompany.services.interfaces.ProductoCompanyService;
import org.frias.avalon.domain.inventory.Producto.modules.adminsaas.dtos.ProductCompanyRequestCreate;
import org.frias.avalon.domain.inventory.Producto.modules.adminsaas.dtos.ProductRequestCreate;
import org.frias.avalon.domain.inventory.Producto.modules.adminsaas.dtos.ProductResponseDto;
import org.frias.avalon.domain.inventory.Producto.modules.adminsaas.services.interfaces.ProductoService;
import org.frias.avalon.domain.outlet.dtos.response.OutletsWhitProductMap;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartRequest;

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

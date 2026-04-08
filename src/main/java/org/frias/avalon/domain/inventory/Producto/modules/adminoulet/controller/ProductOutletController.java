package org.frias.avalon.domain.inventory.Producto.modules.adminoulet.controller;

import org.frias.avalon.domain.inventory.Producto.modules.adminsaas.dtos.ProductCompanyRequestCreate;
import org.frias.avalon.domain.inventory.Producto.modules.adminsaas.dtos.ProductResponseDto;
import org.frias.avalon.domain.outlet.dtos.request.OutletMap;
import org.frias.avalon.domain.outlet.dtos.response.OutletWithCatalogProductResponse;
import org.frias.avalon.domain.outlet.dtos.response.OutletsWhitProductMap;
import org.frias.avalon.domain.inventory.Producto.modules.adminoulet.ProductOutletResponseDto;
import org.frias.avalon.domain.inventory.Producto.modules.adminoulet.services.interfaces.ProductOutletService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/productOutlet")
public class ProductOutletController {

    private final ProductOutletService productOutletService;


    public ProductOutletController(ProductOutletService productOutletService) {
        this.productOutletService = productOutletService;
    }

@GetMapping("/all/catatalog/outlet/{id}")
    public List<ProductOutletResponseDto> getAll(@PathVariable Long id) {

        return productOutletService.getProductCatalogToOutlet(id) ;
}

    @GetMapping("/catalog/{id}")
    public List<ProductOutletResponseDto> getCatalog(@PathVariable Long id) {

        return productOutletService.getProductCatalogToOutlet(id) ;
    }

    @PostMapping("/nearby/by-nameproduct")
    public List<OutletsWhitProductMap> getOutletsByProduct(@RequestBody OutletMap product) {
        return productOutletService.getOutletProductByNameProduct(product);
    }







}

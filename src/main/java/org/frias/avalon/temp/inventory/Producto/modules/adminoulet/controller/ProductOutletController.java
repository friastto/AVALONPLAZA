package org.frias.avalon.temp.inventory.Producto.modules.adminoulet.controller;

import org.frias.avalon.temp.inventory.Producto.modules.adminoulet.ProductOutletResponseDto;
import org.frias.avalon.temp.inventory.Producto.modules.adminoulet.services.interfaces.ProductOutletService;
import org.frias.avalon.temp.empresasucursal.sucursal.dtos.OutletsRequestMap;
import org.frias.avalon.temp.empresasucursal.sucursal.dtos.OutletsWhitProductResponseMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/productOutlet")
public class ProductOutletController {

    private final ProductOutletService productOutletService;


    public ProductOutletController(ProductOutletService productOutletService) {
        this.productOutletService = productOutletService;
    }

@GetMapping("all")
    public List<ProductOutletResponseDto> getAll() {

        return productOutletService.getAllProductCatalog() ;
}

    @GetMapping("/catalog/{id}")
    public List<ProductOutletResponseDto> getCatalog(@PathVariable Long id) {

        return productOutletService.getProductCatalogToOutlet(id) ;
    }

    @PostMapping("/nearby/by-nameproduct")
    public List<OutletsWhitProductResponseMap> getOutletsByProduct(@RequestBody OutletsRequestMap product) {
        return productOutletService.getOutletProductByNameProduct(product);
    }
}

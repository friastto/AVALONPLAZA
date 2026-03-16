package org.frias.avalon.Producto.modules.adminoulet.controller;

import org.frias.avalon.Producto.modules.adminoulet.ProductOutletResponseDto;
import org.frias.avalon.Producto.modules.adminoulet.services.interfaces.ProductOutletService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}

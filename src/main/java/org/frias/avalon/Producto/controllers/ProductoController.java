package org.frias.avalon.Producto.controllers;

import org.frias.avalon.Producto.dtos.ProductRequestCreate;
import org.frias.avalon.Producto.dtos.ProductResponseDto;
import org.frias.avalon.Producto.entities.Product;
import org.frias.avalon.Producto.repositories.ProductRepository;
import org.frias.avalon.Producto.services.interfaces.ProductoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @PostMapping("/add")
    public ProductResponseDto addProduct(@RequestBody ProductRequestCreate product) {

        return productoService.save(product);

    }@GetMapping("/search/v2")
    public ProductResponseDto searchProduct(@RequestParam String codeBar) {

        return productoService.findByCodeBar(codeBar);

    }
    @GetMapping("/findAll")
    public List<ProductResponseDto > getAll() {

        return productoService.findAll();

    }
}

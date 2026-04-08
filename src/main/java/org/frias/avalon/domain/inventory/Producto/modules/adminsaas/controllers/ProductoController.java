package org.frias.avalon.domain.inventory.Producto.modules.adminsaas.controllers;

import org.frias.avalon.core.uploadimg.service.ProductUploadImgImpl;
import org.frias.avalon.domain.inventory.Producto.modules.adminsaas.dtos.ProductRequestCreate;
import org.frias.avalon.domain.inventory.Producto.modules.adminsaas.dtos.ProductResponseDto;
import org.frias.avalon.domain.inventory.Producto.modules.adminsaas.services.interfaces.ProductoService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/avalon/product")
public class ProductoController {

    private final ProductoService productoService;
    private final ProductUploadImgImpl s3Service;


    public ProductoController(ProductoService productoService, ProductUploadImgImpl s3Service) {
        this.productoService = productoService;
        this.s3Service = s3Service;
    }

    @PostMapping(value ="/add",  consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProductResponseDto addProduct(@RequestBody ProductRequestCreate product) {

        return null ;//productoService.save(product);

    }

    @GetMapping("/search/v2")
    public ProductResponseDto searchProduct(@RequestParam String codeBar) {

        return productoService.findByCodeBar(codeBar);

    }
    @GetMapping("/findAll")
    public List<ProductResponseDto > getAll() {

        return productoService.findAll();
    }

    @PostMapping(value = "/productsImg", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProductResponseDto createProduct(
            @RequestPart("data") ProductRequestCreate data,
            @RequestPart("image") MultipartFile image) {

        return productoService.save(data,image);
    }


}

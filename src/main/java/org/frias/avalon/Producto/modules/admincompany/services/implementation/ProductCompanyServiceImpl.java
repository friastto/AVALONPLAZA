package org.frias.avalon.Producto.modules.admincompany.services.implementation;

import jakarta.persistence.EntityExistsException;
import org.frias.avalon.Producto.modules.admincompany.services.dto.BarcodeRequestDto;
import org.frias.avalon.Producto.modules.admincompany.services.dto.BarcodeResponseNewDto;
import org.frias.avalon.Producto.modules.admincompany.services.interfaces.ProductoCompanyService;
import org.frias.avalon.Producto.modules.admincompany.services.repository.BarcodeCompanyRepository;
import org.frias.avalon.Producto.modules.adminsaas.entities.Product;
import org.frias.avalon.Producto.modules.adminsaas.entities.ProductBarcode;
import org.frias.avalon.Producto.modules.adminsaas.services.interfaces.ProductoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductCompanyServiceImpl implements ProductoCompanyService {

    private final BarcodeCompanyRepository barcodeCompanyRepository;
    private final ProductoService productService;
    public ProductCompanyServiceImpl(BarcodeCompanyRepository barcodeCompanyRepository, ProductoService productService) {
        this.barcodeCompanyRepository = barcodeCompanyRepository;
        this.productService = productService;

    }

    @Transactional
    @Override
    public BarcodeResponseNewDto addBarcode(BarcodeRequestDto barcodeRequestDto) {

        barcodeCompanyRepository.existsByBarcode(
                barcodeRequestDto.desc()
        ).ifPresent(
                exists -> {throw new EntityExistsException("Este código ya está vinculado a otro producto");}
            );


        Product product = productService.searchById(barcodeRequestDto.productId());

        ProductBarcode newBarcode = ProductBarcode.builder()
                .barcode(barcodeRequestDto.newBarcode())
                .product(product)
                .description(barcodeRequestDto.desc())
                .build();

        barcodeCompanyRepository.save(newBarcode);

        return null;
    }
}

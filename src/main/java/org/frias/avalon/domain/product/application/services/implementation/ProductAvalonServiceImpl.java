package org.frias.avalon.domain.product.application.services.implementation;

import org.frias.avalon.core.uploadimg.service.ImgProcessorService;
import org.frias.avalon.domain.masterdata.services.interfaces.MasterDataService;
import org.frias.avalon.domain.product.application.services.interfaces.ProductAvalonService;
import org.frias.avalon.domain.product.domain.entity.Product;
import org.frias.avalon.domain.product.infraestructure.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class ProductAvalonServiceImpl implements ProductAvalonService {

    private final ImgProcessorService imgProcessorService;

    private final ProductRepository productRepository;
    private final MasterDataService masterDataService;


    public ProductAvalonServiceImpl(ImgProcessorService imgProcessorService, ProductRepository productRepository, MasterDataService masterDataService) {
        this.imgProcessorService = imgProcessorService;
        this.productRepository = productRepository;
        this.masterDataService = masterDataService;
    }


    @Override
    public Product createProduct(
            String name,
            String description,
            Long categoryId,
            Long unitMeasureId,
            MultipartFile image
    ) {
        String fileName = "PlaceHolderName_temp.webp";
        Product product = new Product();

        product.setName(name);
        product.setDescription(description);
        product.setCategory(masterDataService.searchById(categoryId));
        product.setUnit(masterDataService.searchById(unitMeasureId));
        product.setStatus(masterDataService.getStatusActive());

        product.setImageUrl(fileName);

        Product productSaved = productRepository.save(product);

        byte[] imageBytes;
        try {
            imageBytes = image.getBytes();
        } catch (IOException e) {
            throw new RuntimeException("Error al leer la imagen del producto. Intente de nuevo.");
        }

        String codebarGeneric = "AvbarG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // Ahora sí, lanzas el proceso asíncrono
        imgProcessorService.processProductImage(productSaved.getId(), imageBytes,codebarGeneric,"AVALON");


        return productSaved;

    }
}

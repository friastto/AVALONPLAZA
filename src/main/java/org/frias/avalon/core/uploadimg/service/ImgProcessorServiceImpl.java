package org.frias.avalon.core.uploadimg.service;

import org.frias.avalon.domain.inventory.Producto.modules.admincompany.repository.ProductCompanyRepository;
import org.frias.avalon.domain.inventory.Producto.modules.adminoulet.repository.ProductOutletRepository;
import org.frias.avalon.domain.inventory.Producto.modules.adminsaas.repositories.ProductRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


@Service
public class ImgProcessorServiceImpl implements ImgProcessorService{

    private final ProductUploadImgImpl productUploadImg;
    private final ProductRepository productRepository;
    private final ProductCompanyRepository productCompanyRepository;
    private final ProductOutletRepository productOutletRepository;

    public ImgProcessorServiceImpl(ProductUploadImgImpl productUploadImg, ProductRepository productRepository, ProductCompanyRepository productCompanyRepository, ProductOutletRepository productOutletRepository) {
        this.productUploadImg = productUploadImg;
        this.productRepository = productRepository;
        this.productCompanyRepository = productCompanyRepository;
        this.productOutletRepository = productOutletRepository;
    }


    @Async
    @Override
    public void processProductImage(Long productId, byte[] imgUrl, String barcode, String targetType) {
        // 1. Aquí haces el trabajo pesado (Remove.bg + S3)
        String finalFileName ;

        try {
            finalFileName = productUploadImg.uploadFile(imgUrl, barcode);
        } catch (Exception e) {
            System.err.println("Error procesando imagen para producto " + productId + ": " + e.getMessage());

            finalFileName ="error_placeholder.png";
        }

        // 2. Actualizas el producto ya existente
        // 2. La actualización depende del tipo

        switch (targetType) {
            case "AVALON" -> productRepository.updateImageUrl(productId, finalFileName);
            case "COMPANY" -> productCompanyRepository.updateImageUrl(productId, finalFileName);
            case "OUTLET" -> productOutletRepository.updateImageUrl(productId, finalFileName);
            default -> throw new IllegalArgumentException("el producto esta fuera del perimetro empresarial de avalon");
        }
    }


}

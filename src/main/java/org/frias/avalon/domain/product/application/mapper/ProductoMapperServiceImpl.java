package org.frias.avalon.domain.product.application.mapper;

import org.frias.avalon.core.uploadimg.service.ProductUploadImgImpl;
import org.frias.avalon.domain.product.application.dto.company.ProductResponseDto;
import org.frias.avalon.domain.product.application.dto.saas.ProductAvalonResponseDto;
import org.frias.avalon.domain.product.domain.entity.Product;
import org.frias.avalon.domain.product.domain.entity.ProductBarcode;
import org.frias.avalon.domain.product.domain.entity.ProductCompany;
import org.frias.avalon.domain.product.domain.entity.ProductOutlet;
import org.frias.avalon.domain.promotion.fabric.convertermasa.factory.ConvertFactoryService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProductoMapperServiceImpl implements ProductoMapperService, ProductoCompanyMapperService, ProductoOutletMapperService {

    private final ConvertFactoryService convertFactoryService;
    private final ProductUploadImgImpl s3UrlImage;

    private final Set<String> unitMasaPesable = Set.of("KG", "LB", "GR");

    public ProductoMapperServiceImpl(ConvertFactoryService convertFactoryService, ProductUploadImgImpl s3UrlImage) {
        this.convertFactoryService = convertFactoryService;
        this.s3UrlImage = s3UrlImage;
    }


    @Override
    public ProductAvalonResponseDto toDto(Product p) {

        return new ProductAvalonResponseDto(
                p.getId(),
                p.getName(),
                p.getDescription(),
                p.getCategory().getFullName(),
                p.getUnit().getShortName(),
                p.getStatus().getFullName(),
                "https://productcatalogavalonplaza.s3.us-east-2.amazonaws.com/"+p.getImageUrl()
        );

    }

    @Override
    public ProductResponseDto toDto(ProductCompany p) {
// 1. Manejo seguro de la lista de barcodes
        String barcodesFormatted = Optional.ofNullable(p.getBarcodes())
                .map(list -> list.stream()
                        .map(ProductBarcode::getBarcode)
                        .filter(bc -> bc != null && !bc.isBlank())
                        .collect(Collectors.joining(", ")))
                .orElse(""); // Si la lista es nula o el stream termina vacío, devuelve ""

        return   new ProductResponseDto(
                p.getId(),

              barcodesFormatted,

                p.getCustomName() != null ? p.getCustomName() : p.getProduct().getName(),
                p.getCustomDescription() != null ? p.getCustomDescription(): p.getProduct().getDescription(),
                p.getCustomPrice() != null ? p.getCustomPrice() :new BigDecimal("0.00") ,
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                p.getProduct().getCategory().getFullName(),
                p.getProduct().getUnit().getShortName(),
                "0 : UND or 0.00 : MASA",
                p.getCustomImageUrl() != null ? p.getCustomImageUrl():p.getProduct().getImageUrl()
        );



    }
    @Override
    public ProductResponseDto toDto(ProductOutlet p) {
// 1. Manejo seguro de la lista de barcodes
        String barcodesFormatted = Optional.ofNullable(p.getCompanyProduct().getBarcodes())
                .map(list -> list.stream()
                        .map(ProductBarcode::getBarcode)
                        .filter(bc -> bc != null && !bc.isBlank())
                        .collect(Collectors.joining(", ")))
                .orElse(""); // Si la lista es nula o el stream termina vacío, devuelve ""

//en este punto se valida que halla un name escalando hasta encontrar uno la outlet->company->productAvalon
        String localName = p.getLocalName() != null
                ? p.getLocalName()
                : p.getCompanyProduct().getCustomName() != null ? p.getCompanyProduct().getCustomName()
                : p.getCompanyProduct().getProduct().getName();

        String localDescription =  p.getLocalDescription() != null
                        ? p.getLocalDescription()
                        : p.getCompanyProduct().getCustomDescription() != null
                        ? p.getCompanyProduct().getCustomDescription()
                        : p.getCompanyProduct().getProduct().getDescription();

        BigDecimal LocalPrice =  p.getLocalPrice() != null
                        ? p.getLocalPrice()
                            : p.getCompanyProduct().getCustomPrice();


        String localImageUrl =  p.getLocalImageUrl() != null
                    ? p.getLocalImageUrl()
                    : p.getCompanyProduct().getCustomImageUrl() != null
                        ? p.getCompanyProduct().getCustomImageUrl()
                        : p.getCompanyProduct().getProduct().getImageUrl();


        String localUnitType = p.getCompanyProduct().getProduct().getUnit().getShortName();

        String quantity = p.getStock().toString();

        String imageUrl = "https://productcatalogavalonplaza.s3.us-east-2.amazonaws.com/"+localImageUrl; //s3UrlImage.getPresignedUrl(localImageUrl);


        if (unitMasaPesable.contains(localUnitType))
            quantity = convertFactoryService.convertTo(
                    p.getStock().toString()
                    , localUnitType
                    , true
            ).toString();

        return   new ProductResponseDto(
                p.getId(),
                barcodesFormatted,
                localName,
                localDescription,
                LocalPrice,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                p.getCompanyProduct().getProduct().getCategory().getFullName(),
                localUnitType,
                p.getStock().toString(),
                imageUrl

        );



    }
}

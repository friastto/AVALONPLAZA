package org.frias.avalon.domain.product.infraestructure.mapper;

import org.frias.avalon.domain.product.application.dto.BarcodeDto;
import org.frias.avalon.domain.product.domain.BarcodeDomain;
import org.frias.avalon.domain.product.infraestructure.entity.Barcode;
import org.springframework.stereotype.Component;

@Component
public class BarcodeMapperImpl implements BarcodeMapper{


    @Override
    public BarcodeDto toDto(Barcode barcode) {
        return new BarcodeDto(
                barcode.getId(),
                barcode.getBarcode(),
                barcode.getProductOutlet(),
                barcode.getDescription()
        );
    }

    @Override
    public BarcodeDomain toDomain(Barcode barcode) {
        return BarcodeDomain.fromPersistence(
                barcode.getId(),
                barcode.getBarcode(),
                barcode.getProductOutlet(),
                barcode.getDescription(),
                barcode.getCreatedAt(),
                barcode.getUpdatedAt()
        );
    }

    @Override
    public Barcode toEntity(BarcodeDomain barcodeDomain) {
        return Barcode.builder()
                .id(barcodeDomain.getId())
                .barcode(barcodeDomain.getBarcode())
                .productOutlet(barcodeDomain.getProductOutletId())
                .description(barcodeDomain.getDescription())
                .createdAt(barcodeDomain.getCreatedAt())
                .updatedAt(barcodeDomain.getUpdatedAt())
                .build();
    }
}
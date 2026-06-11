package org.frias.avalon.domain.product.infraestructure.mapper;

import org.frias.avalon.domain.product.application.dto.BarcodeDto;
import org.frias.avalon.domain.product.domain.BarcodeDomain;
import org.frias.avalon.domain.product.infraestructure.entity.Barcode;

public interface BarcodeMapper {
    BarcodeDto toDto(Barcode barcode);
    BarcodeDomain toDomain(Barcode barcode);
    Barcode toEntity(BarcodeDomain barcodeDomain);
}

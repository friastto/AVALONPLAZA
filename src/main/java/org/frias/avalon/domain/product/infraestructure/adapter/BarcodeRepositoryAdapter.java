package org.frias.avalon.domain.product.infraestructure.adapter;

import lombok.RequiredArgsConstructor;
import org.frias.avalon.domain.product.domain.BarcodeDomain;
import org.frias.avalon.domain.product.domain.repository.BarcodeRepositoryPort;
import org.frias.avalon.domain.product.infraestructure.entity.Barcode;
import org.frias.avalon.domain.product.infraestructure.mapper.BarcodeMapper;
import org.frias.avalon.domain.product.infraestructure.repository.JpaBarcodeRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class BarcodeRepositoryAdapter implements BarcodeRepositoryPort {

    private final JpaBarcodeRepository jpaBarcodeRepository;
    private final BarcodeMapper barcodeMapper;

    @Override
    public BarcodeDomain save(BarcodeDomain barcodeDomain) {
        Barcode barcodeEntity = barcodeMapper.toEntity(barcodeDomain);
        Barcode savedEntity = jpaBarcodeRepository.save(barcodeEntity);
        return barcodeMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<BarcodeDomain> findByCode(String code) {
        return jpaBarcodeRepository.findByBarcode(code)
                .map(barcodeMapper::toDomain);
    }
}
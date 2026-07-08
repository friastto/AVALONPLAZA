package org.frias.avalon.domain.product.infraestructure.mapper;

import lombok.RequiredArgsConstructor;
import org.frias.avalon.domain.masterdata.application.dto.response.MasterDataResponseDto;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.masterdata.infraestructure.mapper.MasterDataMapperService;
import org.frias.avalon.domain.product.application.dto.response.ProductResponse;
import org.frias.avalon.domain.product.domain.ProductDomain;
import org.frias.avalon.domain.product.domain.service.UnitConversionService;
import org.frias.avalon.domain.product.infraestructure.entity.ProductOutlet;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductOutletMapperImpl implements ProductOutletMapper {

    private final MasterTreeProvider masterTreeProvider;
    private final MasterDataMapperService masterDataMapper;
    private final UnitConversionService unitConversionService;

    @Override
    public ProductDomain toDomain(ProductOutlet entity) {
        if (entity == null) {
            return null;
        }
        return ProductDomain.fromPersistence(
                entity.getId(),
                entity.getLocalName(),
                entity.getLocalDescription(),
                entity.getStock(),
                entity.getUnitMeasureId(), // Mapeamos el unitMeasureId
                entity.getLocalImageUrl() != null ? String.join(",", entity.getLocalImageUrl()) : null,
                entity.getLocalPrice(),
                entity.getOutletId(),
                entity.getStatusId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    @Override
    public ProductOutlet toEntity(ProductDomain domain) {
        if (domain == null) {
            return null;
        }
        return ProductOutlet.builder()
                .id(domain.getId())
                .localName(domain.getName())
                .localDescription(domain.getDescription())
                .stock(domain.getStock())
                .unitMeasureId(domain.getUnitMeasureId()) // Mapeamos el unitMeasureId
                .localImageUrl(domain.getImageUrl() != null && !domain.getImageUrl().isBlank() ? java.util.Arrays.asList(domain.getImageUrl().split(",")) : java.util.Collections.emptyList())
                .localPrice(domain.getPrice())
                .outletId(domain.getOutletId())
                .statusId(domain.getStatusId())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    @Override
    public ProductResponse toResponse(ProductDomain domain) {
        return toResponse(domain, null);
    }

    @Override
    public ProductResponse toResponse(ProductDomain domain, String barCode) {
        if (domain == null) {
            return null;
        }

        // 1. Enriquecer el DTO con el estado
        MasterRoot statusNode = masterTreeProvider.getTree().getById(domain.getStatusId());
        MasterDataResponseDto statusDto = masterDataMapper.toResponse(statusNode);

        // 2. Enriquecer el DTO con el stock formateado
        // Ahora que tenemos el unitMeasureId en el dominio, usamos el servicio de conversión inversa
        String displayStock = unitConversionService.convertFromSmallestUnit(domain.getStock(), domain.getUnitMeasureId());

        return new ProductResponse(
                domain.getId(),
                domain.getName(),
                domain.getDescription(),
                displayStock, // Usamos el stock formateado por el servicio
                domain.getImageUrl(),
                domain.getPrice(),
                domain.getOutletId(),
                statusDto,
                barCode,
                domain.getUnitMeasureId(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }
}

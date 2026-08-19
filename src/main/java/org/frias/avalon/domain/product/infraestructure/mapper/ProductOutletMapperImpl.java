package org.frias.avalon.domain.product.infraestructure.mapper;

import lombok.RequiredArgsConstructor;
import org.frias.avalon.core.permissions.CurrentUserProviderPort;
import org.frias.avalon.core.permissions.UserContext;
import org.frias.avalon.domain.masterdata.application.dto.response.MasterDataResponseDto;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.service.MasterTreeProvider;
import org.frias.avalon.domain.masterdata.domain.repository.MasterDataRepositoryPort;
import org.frias.avalon.domain.masterdata.infraestructure.mapper.MasterDataMapperService;
import org.frias.avalon.domain.order.infrastructure.persistence.repository.JpaOrderRepository;
import org.frias.avalon.domain.product.application.dto.response.ProductResponse;
import org.frias.avalon.domain.product.domain.ProductDomain;
import org.frias.avalon.domain.product.domain.service.UnitConversionService;
import org.frias.avalon.domain.product.infraestructure.entity.ProductOutlet;
import org.frias.avalon.domain.user.domain.model.UserAvalonDomain;
import org.frias.avalon.domain.user.domain.port.UserAvalonRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProductOutletMapperImpl implements ProductOutletMapper {

    private final MasterTreeProvider masterTreeProvider;
    private final MasterDataMapperService masterDataMapper;
    private final UnitConversionService unitConversionService;
    private final JpaOrderRepository jpaOrderRepository;
    private final CurrentUserProviderPort currentUserProvider;
    private final UserAvalonRepositoryPort userAvalonRepositoryPort;
    private final MasterDataRepositoryPort masterDataRepositoryPort;

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
                entity.getUnitMeasureId(),
                entity.getLocalImageUrl() != null ? String.join(",", entity.getLocalImageUrl()) : null,
                entity.getLocalPrice(),
                entity.getOutletId(),
                entity.getStatusId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getVersion()
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
                .unitMeasureId(domain.getUnitMeasureId())
                .localImageUrl(domain.getImageUrl() != null && !domain.getImageUrl().isBlank() ? java.util.Arrays.asList(domain.getImageUrl().split(",")) : java.util.Collections.emptyList())
                .localPrice(domain.getPrice())
                .outletId(domain.getOutletId())
                .statusId(domain.getStatusId())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .version(domain.getVersion())
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
        String displayStock = unitConversionService.convertFromSmallestUnit(domain.getStock(), domain.getUnitMeasureId());

        // 3. Calculo dinamico de unidades apartadas por tienda (Global) y por el usuario actual
        Integer reservedGlobalUnits = 0;
        Integer reservedUserUnits = 0;
        try {
            List<Long> activeStatusIds = new ArrayList<>();
            Long ordPenId = masterDataRepositoryPort.getIdByCode("ORD_PEN");
            if (ordPenId != null) activeStatusIds.add(ordPenId);
            Long ordRecId = masterDataRepositoryPort.getIdByCode("ORD_REC");
            if (ordRecId != null) activeStatusIds.add(ordRecId);
            Long penId = masterDataRepositoryPort.getIdByCode("PEN");
            if (penId != null && !activeStatusIds.contains(penId)) activeStatusIds.add(penId);
            if (activeStatusIds.isEmpty()) {
                activeStatusIds = List.of(1L, 2L);
            }

            reservedGlobalUnits = jpaOrderRepository.sumQuantityByProductOutletIdAndStatusIn(domain.getId(), activeStatusIds);
            if (reservedGlobalUnits == null) reservedGlobalUnits = 0;

            UserContext userCtx = currentUserProvider.getCurrentUserContext();
            if (userCtx != null && userCtx.username() != null) {
                Optional<UserAvalonDomain> userOpt = userAvalonRepositoryPort.findByUserName(userCtx.username());
                if (userOpt.isPresent()) {
                    Long customerId = userOpt.get().getId();
                    Integer userSum = jpaOrderRepository.sumQuantityByProductOutletIdAndCustomerIdAndStatusIn(domain.getId(), customerId, activeStatusIds);
                    if (userSum != null) {
                        reservedUserUnits = userSum;
                    }
                }
            }
        } catch (Exception e) {
            // Silencioso si la consulta no retorna datos
        }

        String displayReservedGlobal = unitConversionService.convertFromSmallestUnit(reservedGlobalUnits, domain.getUnitMeasureId());
        String displayReservedUser = unitConversionService.convertFromSmallestUnit(reservedUserUnits, domain.getUnitMeasureId());

        // Resolucion de imagen en 3 niveles: L3 (local tienda) > L2 (empresa) > L1 (Avalon global)
        String localImg = domain.getImageUrl();
        String effectiveImageUrl = (localImg != null && !localImg.isBlank()) ? localImg.split(",")[0].trim() : null;

        return new ProductResponse(
                domain.getId(),
                domain.getName(),
                domain.getDescription(),
                displayStock,
                displayReservedGlobal,
                displayReservedUser,
                domain.getImageUrl(),
                effectiveImageUrl,
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

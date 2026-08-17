package org.frias.avalon.domain.claim.infrastructure.persistence.mapper;

import org.frias.avalon.domain.claim.application.dto.response.ClaimItemResponse;
import org.frias.avalon.domain.claim.application.dto.response.ClaimResponse;
import org.frias.avalon.domain.claim.domain.OrderClaimDomain;
import org.frias.avalon.domain.claim.domain.OrderClaimItemDomain;
import org.frias.avalon.domain.claim.domain.OrderClaimPhotoDomain;
import org.frias.avalon.domain.claim.infrastructure.persistence.entity.OrderClaimEntity;
import org.frias.avalon.domain.claim.infrastructure.persistence.entity.OrderClaimItemEntity;
import org.frias.avalon.domain.claim.infrastructure.persistence.entity.OrderClaimPhotoEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ClaimMapper {

    public OrderClaimDomain toDomain(OrderClaimEntity entity, List<OrderClaimItemEntity> itemEntities, List<OrderClaimPhotoEntity> photoEntities) {
        if (entity == null) return null;
        List<OrderClaimItemDomain> items = itemEntities != null
                ? itemEntities.stream().map(this::toItemDomain).collect(Collectors.toList())
                : List.of();

        List<OrderClaimPhotoDomain> photos = photoEntities != null
                ? photoEntities.stream().map(this::toPhotoDomain).collect(Collectors.toList())
                : List.of();

        return OrderClaimDomain.builder()
                .id(entity.getId())
                .orderId(entity.getOrderId())
                .customerId(entity.getCustomerId())
                .claimTypeId(entity.getClaimTypeId())
                .statusId(entity.getStatusId())
                .description(entity.getDescription())
                .resolutionNotes(entity.getResolutionNotes())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .items(items)
                .photos(photos)
                .build();
    }

    public OrderClaimEntity toEntity(OrderClaimDomain domain) {
        if (domain == null) return null;
        return OrderClaimEntity.builder()
                .id(domain.getId())
                .orderId(domain.getOrderId())
                .customerId(domain.getCustomerId())
                .claimTypeId(domain.getClaimTypeId())
                .statusId(domain.getStatusId())
                .description(domain.getDescription())
                .resolutionNotes(domain.getResolutionNotes())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    public OrderClaimItemDomain toItemDomain(OrderClaimItemEntity entity) {
        if (entity == null) return null;
        return OrderClaimItemDomain.builder()
                .id(entity.getId())
                .claimId(entity.getClaimId())
                .orderItemId(entity.getOrderItemId())
                .quantityAffected(entity.getQuantityAffected())
                .reason(entity.getReason())
                .build();
    }

    public OrderClaimItemEntity toItemEntity(OrderClaimItemDomain domain) {
        if (domain == null) return null;
        return OrderClaimItemEntity.builder()
                .id(domain.getId())
                .claimId(domain.getClaimId())
                .orderItemId(domain.getOrderItemId())
                .quantityAffected(domain.getQuantityAffected())
                .reason(domain.getReason())
                .build();
    }

    public OrderClaimPhotoDomain toPhotoDomain(OrderClaimPhotoEntity entity) {
        if (entity == null) return null;
        return OrderClaimPhotoDomain.builder()
                .id(entity.getId())
                .claimId(entity.getClaimId())
                .photoUrl(entity.getPhotoUrl())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public OrderClaimPhotoEntity toPhotoEntity(OrderClaimPhotoDomain domain) {
        if (domain == null) return null;
        return OrderClaimPhotoEntity.builder()
                .id(domain.getId())
                .claimId(domain.getClaimId())
                .photoUrl(domain.getPhotoUrl())
                .createdAt(domain.getCreatedAt())
                .build();
    }

    public ClaimItemResponse toItemResponse(OrderClaimItemDomain domain) {
        if (domain == null) return null;
        return ClaimItemResponse.builder()
                .id(domain.getId())
                .orderItemId(domain.getOrderItemId())
                .quantityAffected(domain.getQuantityAffected())
                .reason(domain.getReason())
                .build();
    }

    public ClaimResponse toResponse(OrderClaimDomain domain) {
        if (domain == null) return null;
        List<ClaimItemResponse> itemResponses = domain.getItems() != null
                ? domain.getItems().stream().map(this::toItemResponse).collect(Collectors.toList())
                : List.of();

        List<String> photoUrls = domain.getPhotos() != null
                ? domain.getPhotos().stream().map(OrderClaimPhotoDomain::getPhotoUrl).collect(Collectors.toList())
                : List.of();

        return ClaimResponse.builder()
                .id(domain.getId())
                .orderId(domain.getOrderId())
                .customerId(domain.getCustomerId())
                .claimTypeId(domain.getClaimTypeId())
                .statusId(domain.getStatusId())
                .description(domain.getDescription())
                .resolutionNotes(domain.getResolutionNotes())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .items(itemResponses)
                .photoUrls(photoUrls)
                .build();
    }
}

package org.frias.avalon.domain.pqrs.infrastructure.persistence.mapper;

import org.frias.avalon.domain.pqrs.domain.PqrsDomain;
import org.frias.avalon.domain.pqrs.infrastructure.persistence.entity.PqrsEntity;
import org.springframework.stereotype.Component;

@Component
public class PqrsMapper {

    public PqrsDomain toDomain(PqrsEntity entity) {
        if (entity == null) return null;
        return PqrsDomain.builder()
                .id(entity.getId())
                .ticketNumber(entity.getTicketNumber())
                .userId(entity.getUserId())
                .orderId(entity.getOrderId())
                .storeId(entity.getStoreId())
                .typeCode(entity.getTypeCode())
                .statusCode(entity.getStatusCode())
                .subject(entity.getSubject())
                .description(entity.getDescription())
                .contactEmail(entity.getContactEmail())
                .contactPhone(entity.getContactPhone())
                .adminNotes(entity.getAdminNotes())
                .respondedByUserId(entity.getRespondedByUserId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public PqrsEntity toEntity(PqrsDomain domain) {
        if (domain == null) return null;
        return PqrsEntity.builder()
                .id(domain.getId())
                .ticketNumber(domain.getTicketNumber())
                .userId(domain.getUserId())
                .orderId(domain.getOrderId())
                .storeId(domain.getStoreId())
                .typeCode(domain.getTypeCode())
                .statusCode(domain.getStatusCode())
                .subject(domain.getSubject())
                .description(domain.getDescription())
                .contactEmail(domain.getContactEmail())
                .contactPhone(domain.getContactPhone())
                .adminNotes(domain.getAdminNotes())
                .respondedByUserId(domain.getRespondedByUserId())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}

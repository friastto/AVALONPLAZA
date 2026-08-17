package org.frias.avalon.domain.claim.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.frias.avalon.domain.claim.application.port.ClaimRepositoryPort;
import org.frias.avalon.domain.claim.domain.OrderClaimDomain;
import org.frias.avalon.domain.claim.domain.OrderClaimItemDomain;
import org.frias.avalon.domain.claim.domain.OrderClaimPhotoDomain;
import org.frias.avalon.domain.claim.infrastructure.persistence.entity.OrderClaimEntity;
import org.frias.avalon.domain.claim.infrastructure.persistence.entity.OrderClaimItemEntity;
import org.frias.avalon.domain.claim.infrastructure.persistence.entity.OrderClaimPhotoEntity;
import org.frias.avalon.domain.claim.infrastructure.persistence.mapper.ClaimMapper;
import org.frias.avalon.domain.claim.infrastructure.persistence.repository.JpaOrderClaimItemRepository;
import org.frias.avalon.domain.claim.infrastructure.persistence.repository.JpaOrderClaimPhotoRepository;
import org.frias.avalon.domain.claim.infrastructure.persistence.repository.JpaOrderClaimRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ClaimPersistenceAdapter implements ClaimRepositoryPort {

    private final JpaOrderClaimRepository jpaOrderClaimRepository;
    private final JpaOrderClaimItemRepository jpaOrderClaimItemRepository;
    private final JpaOrderClaimPhotoRepository jpaOrderClaimPhotoRepository;
    private final ClaimMapper claimMapper;

    @Override
    public OrderClaimDomain save(OrderClaimDomain claim) {
        OrderClaimEntity entity = claimMapper.toEntity(claim);
        OrderClaimEntity savedEntity = jpaOrderClaimRepository.save(entity);

        if (claim.getItems() != null && !claim.getItems().isEmpty()) {
            for (OrderClaimItemDomain itemDomain : claim.getItems()) {
                itemDomain.setClaimId(savedEntity.getId());
                OrderClaimItemEntity itemEntity = claimMapper.toItemEntity(itemDomain);
                jpaOrderClaimItemRepository.save(itemEntity);
            }
        }

        if (claim.getPhotos() != null && !claim.getPhotos().isEmpty()) {
            for (OrderClaimPhotoDomain photoDomain : claim.getPhotos()) {
                photoDomain.setClaimId(savedEntity.getId());
                OrderClaimPhotoEntity photoEntity = claimMapper.toPhotoEntity(photoDomain);
                jpaOrderClaimPhotoRepository.save(photoEntity);
            }
        }

        List<OrderClaimItemEntity> itemEntities = jpaOrderClaimItemRepository.findAllByClaimId(savedEntity.getId());
        List<OrderClaimPhotoEntity> photoEntities = jpaOrderClaimPhotoRepository.findAllByClaimId(savedEntity.getId());

        return claimMapper.toDomain(savedEntity, itemEntities, photoEntities);
    }

    @Override
    public Optional<OrderClaimDomain> findById(Long id) {
        return jpaOrderClaimRepository.findById(id).map(entity -> {
            List<OrderClaimItemEntity> itemEntities = jpaOrderClaimItemRepository.findAllByClaimId(entity.getId());
            List<OrderClaimPhotoEntity> photoEntities = jpaOrderClaimPhotoRepository.findAllByClaimId(entity.getId());
            return claimMapper.toDomain(entity, itemEntities, photoEntities);
        });
    }

    @Override
    public List<OrderClaimDomain> findAllByOrderId(Long orderId) {
        return jpaOrderClaimRepository.findAllByOrderId(orderId).stream().map(entity -> {
            List<OrderClaimItemEntity> itemEntities = jpaOrderClaimItemRepository.findAllByClaimId(entity.getId());
            List<OrderClaimPhotoEntity> photoEntities = jpaOrderClaimPhotoRepository.findAllByClaimId(entity.getId());
            return claimMapper.toDomain(entity, itemEntities, photoEntities);
        }).collect(Collectors.toList());
    }

    @Override
    public List<OrderClaimDomain> findAllByCustomerId(Long customerId) {
        return jpaOrderClaimRepository.findAllByCustomerId(customerId).stream().map(entity -> {
            List<OrderClaimItemEntity> itemEntities = jpaOrderClaimItemRepository.findAllByClaimId(entity.getId());
            List<OrderClaimPhotoEntity> photoEntities = jpaOrderClaimPhotoRepository.findAllByClaimId(entity.getId());
            return claimMapper.toDomain(entity, itemEntities, photoEntities);
        }).collect(Collectors.toList());
    }
}

package org.frias.avalon.domain.claim.infrastructure.persistence.adapter;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Unit Tests for ClaimPersistenceAdapter in PQRS Claim Domain")
class ClaimPersistenceAdapterTest {

    private JpaOrderClaimRepository jpaOrderClaimRepository;
    private JpaOrderClaimItemRepository jpaOrderClaimItemRepository;
    private JpaOrderClaimPhotoRepository jpaOrderClaimPhotoRepository;
    private ClaimMapper claimMapper;

    private ClaimPersistenceAdapter claimPersistenceAdapter;

    @BeforeEach
    void setUp() {
        jpaOrderClaimRepository = mock(JpaOrderClaimRepository.class);
        jpaOrderClaimItemRepository = mock(JpaOrderClaimItemRepository.class);
        jpaOrderClaimPhotoRepository = mock(JpaOrderClaimPhotoRepository.class);
        claimMapper = mock(ClaimMapper.class);

        claimPersistenceAdapter = new ClaimPersistenceAdapter(
                jpaOrderClaimRepository,
                jpaOrderClaimItemRepository,
                jpaOrderClaimPhotoRepository,
                claimMapper
        );
    }

    @Test
    @DisplayName("Should save claim with items and photos, setting claimId and returning mapped domain")
    void save_WithItemsAndPhotos_SavesAllAndReturnsMappedDomain() {
        OrderClaimItemDomain itemDomain = OrderClaimItemDomain.builder()
                .orderItemId(100L)
                .quantityAffected(2)
                .reason("Producto roto")
                .build();

        OrderClaimPhotoDomain photoDomain = OrderClaimPhotoDomain.builder()
                .photoUrl("https://cdn.avalon.com/claims/p1.jpg")
                .createdAt(LocalDateTime.now())
                .build();

        OrderClaimDomain inputClaim = OrderClaimDomain.builder()
                .orderId(50L)
                .customerId(10L)
                .claimTypeId(1L)
                .description("Reclamo por empaque roto")
                .items(List.of(itemDomain))
                .photos(List.of(photoDomain))
                .build();

        OrderClaimEntity mappedClaimEntity = OrderClaimEntity.builder()
                .orderId(50L)
                .customerId(10L)
                .claimTypeId(1L)
                .description("Reclamo por empaque roto")
                .build();

        OrderClaimEntity savedClaimEntity = OrderClaimEntity.builder()
                .id(1L)
                .orderId(50L)
                .customerId(10L)
                .claimTypeId(1L)
                .description("Reclamo por empaque roto")
                .build();

        OrderClaimItemEntity mappedItemEntity = OrderClaimItemEntity.builder()
                .claimId(1L)
                .orderItemId(100L)
                .quantityAffected(2)
                .reason("Producto roto")
                .build();

        OrderClaimItemEntity savedItemEntity = OrderClaimItemEntity.builder()
                .id(10L)
                .claimId(1L)
                .orderItemId(100L)
                .quantityAffected(2)
                .reason("Producto roto")
                .build();

        OrderClaimPhotoEntity mappedPhotoEntity = OrderClaimPhotoEntity.builder()
                .claimId(1L)
                .photoUrl("https://cdn.avalon.com/claims/p1.jpg")
                .build();

        OrderClaimPhotoEntity savedPhotoEntity = OrderClaimPhotoEntity.builder()
                .id(20L)
                .claimId(1L)
                .photoUrl("https://cdn.avalon.com/claims/p1.jpg")
                .build();

        OrderClaimDomain expectedDomain = OrderClaimDomain.builder()
                .id(1L)
                .orderId(50L)
                .customerId(10L)
                .description("Reclamo por empaque roto")
                .items(List.of(itemDomain))
                .photos(List.of(photoDomain))
                .build();

        when(claimMapper.toEntity(inputClaim)).thenReturn(mappedClaimEntity);
        when(jpaOrderClaimRepository.save(mappedClaimEntity)).thenReturn(savedClaimEntity);
        when(claimMapper.toItemEntity(itemDomain)).thenReturn(mappedItemEntity);
        when(jpaOrderClaimItemRepository.save(mappedItemEntity)).thenReturn(savedItemEntity);
        when(claimMapper.toPhotoEntity(photoDomain)).thenReturn(mappedPhotoEntity);
        when(jpaOrderClaimPhotoRepository.save(mappedPhotoEntity)).thenReturn(savedPhotoEntity);

        when(jpaOrderClaimItemRepository.findAllByClaimId(1L)).thenReturn(List.of(savedItemEntity));
        when(jpaOrderClaimPhotoRepository.findAllByClaimId(1L)).thenReturn(List.of(savedPhotoEntity));
        when(claimMapper.toDomain(savedClaimEntity, List.of(savedItemEntity), List.of(savedPhotoEntity)))
                .thenReturn(expectedDomain);

        OrderClaimDomain result = claimPersistenceAdapter.save(inputClaim);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(1L, itemDomain.getClaimId());
        assertEquals(1L, photoDomain.getClaimId());

        verify(jpaOrderClaimRepository, times(1)).save(mappedClaimEntity);
        verify(jpaOrderClaimItemRepository, times(1)).save(mappedItemEntity);
        verify(jpaOrderClaimPhotoRepository, times(1)).save(mappedPhotoEntity);
        verify(jpaOrderClaimItemRepository, times(1)).findAllByClaimId(1L);
        verify(jpaOrderClaimPhotoRepository, times(1)).findAllByClaimId(1L);
    }

    @Test
    @DisplayName("Should save claim without items or photos cleanly")
    void save_WithoutItemsAndPhotos_SavesClaimAndReturnsMappedDomain() {
        OrderClaimDomain inputClaim = OrderClaimDomain.builder()
                .orderId(60L)
                .customerId(12L)
                .description("Consulta sin items")
                .items(null)
                .photos(Collections.emptyList())
                .build();

        OrderClaimEntity mappedEntity = OrderClaimEntity.builder().orderId(60L).build();
        OrderClaimEntity savedEntity = OrderClaimEntity.builder().id(2L).orderId(60L).build();
        OrderClaimDomain expectedDomain = OrderClaimDomain.builder().id(2L).orderId(60L).build();

        when(claimMapper.toEntity(inputClaim)).thenReturn(mappedEntity);
        when(jpaOrderClaimRepository.save(mappedEntity)).thenReturn(savedEntity);
        when(jpaOrderClaimItemRepository.findAllByClaimId(2L)).thenReturn(Collections.emptyList());
        when(jpaOrderClaimPhotoRepository.findAllByClaimId(2L)).thenReturn(Collections.emptyList());
        when(claimMapper.toDomain(savedEntity, Collections.emptyList(), Collections.emptyList()))
                .thenReturn(expectedDomain);

        OrderClaimDomain result = claimPersistenceAdapter.save(inputClaim);

        assertNotNull(result);
        assertEquals(2L, result.getId());
        verify(jpaOrderClaimItemRepository, never()).save(any());
        verify(jpaOrderClaimPhotoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should find claim by ID when present")
    void findById_WhenExists_ReturnsMappedDomain() {
        Long claimId = 5L;
        OrderClaimEntity entity = OrderClaimEntity.builder().id(claimId).build();
        OrderClaimItemEntity itemEntity = OrderClaimItemEntity.builder().id(100L).claimId(claimId).build();
        OrderClaimPhotoEntity photoEntity = OrderClaimPhotoEntity.builder().id(200L).claimId(claimId).build();
        OrderClaimDomain expectedDomain = OrderClaimDomain.builder().id(claimId).build();

        when(jpaOrderClaimRepository.findById(claimId)).thenReturn(Optional.of(entity));
        when(jpaOrderClaimItemRepository.findAllByClaimId(claimId)).thenReturn(List.of(itemEntity));
        when(jpaOrderClaimPhotoRepository.findAllByClaimId(claimId)).thenReturn(List.of(photoEntity));
        when(claimMapper.toDomain(entity, List.of(itemEntity), List.of(photoEntity))).thenReturn(expectedDomain);

        Optional<OrderClaimDomain> result = claimPersistenceAdapter.findById(claimId);

        assertTrue(result.isPresent());
        assertEquals(claimId, result.get().getId());
    }

    @Test
    @DisplayName("Should return empty optional when findById misses")
    void findById_WhenNotExists_ReturnsEmptyOptional() {
        Long claimId = 999L;
        when(jpaOrderClaimRepository.findById(claimId)).thenReturn(Optional.empty());

        Optional<OrderClaimDomain> result = claimPersistenceAdapter.findById(claimId);

        assertTrue(result.isEmpty());
        verify(jpaOrderClaimItemRepository, never()).findAllByClaimId(any());
        verify(jpaOrderClaimPhotoRepository, never()).findAllByClaimId(any());
    }

    @Test
    @DisplayName("Should find all claims by order ID")
    void findAllByOrderId_ReturnsMappedDomainList() {
        Long orderId = 50L;
        OrderClaimEntity entity1 = OrderClaimEntity.builder().id(1L).orderId(orderId).build();
        OrderClaimEntity entity2 = OrderClaimEntity.builder().id(2L).orderId(orderId).build();

        OrderClaimDomain domain1 = OrderClaimDomain.builder().id(1L).orderId(orderId).build();
        OrderClaimDomain domain2 = OrderClaimDomain.builder().id(2L).orderId(orderId).build();

        when(jpaOrderClaimRepository.findAllByOrderId(orderId)).thenReturn(List.of(entity1, entity2));
        when(jpaOrderClaimItemRepository.findAllByClaimId(1L)).thenReturn(Collections.emptyList());
        when(jpaOrderClaimPhotoRepository.findAllByClaimId(1L)).thenReturn(Collections.emptyList());
        when(jpaOrderClaimItemRepository.findAllByClaimId(2L)).thenReturn(Collections.emptyList());
        when(jpaOrderClaimPhotoRepository.findAllByClaimId(2L)).thenReturn(Collections.emptyList());

        when(claimMapper.toDomain(entity1, Collections.emptyList(), Collections.emptyList())).thenReturn(domain1);
        when(claimMapper.toDomain(entity2, Collections.emptyList(), Collections.emptyList())).thenReturn(domain2);

        List<OrderClaimDomain> results = claimPersistenceAdapter.findAllByOrderId(orderId);

        assertEquals(2, results.size());
        assertEquals(1L, results.get(0).getId());
        assertEquals(2L, results.get(1).getId());
    }

    @Test
    @DisplayName("Should find all claims by customer ID")
    void findAllByCustomerId_ReturnsMappedDomainList() {
        Long customerId = 77L;
        OrderClaimEntity entity = OrderClaimEntity.builder().id(3L).customerId(customerId).build();
        OrderClaimDomain domain = OrderClaimDomain.builder().id(3L).customerId(customerId).build();

        when(jpaOrderClaimRepository.findAllByCustomerId(customerId)).thenReturn(List.of(entity));
        when(jpaOrderClaimItemRepository.findAllByClaimId(3L)).thenReturn(Collections.emptyList());
        when(jpaOrderClaimPhotoRepository.findAllByClaimId(3L)).thenReturn(Collections.emptyList());
        when(claimMapper.toDomain(entity, Collections.emptyList(), Collections.emptyList())).thenReturn(domain);

        List<OrderClaimDomain> results = claimPersistenceAdapter.findAllByCustomerId(customerId);

        assertEquals(1, results.size());
        assertEquals(3L, results.get(0).getId());
    }
}

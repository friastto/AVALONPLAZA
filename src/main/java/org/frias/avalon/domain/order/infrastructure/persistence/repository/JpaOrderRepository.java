package org.frias.avalon.domain.order.infrastructure.persistence.repository;

import org.frias.avalon.domain.order.infrastructure.persistence.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

@Repository("omnichannelJpaOrderRepository")
public interface JpaOrderRepository extends JpaRepository<OrderEntity, Long> {
    Optional<OrderEntity> findByOrderCode(String orderCode);

    @Query("SELECT o FROM OrderEntity o WHERE o.outletId = :outletId AND o.orderStatusId = :statusId AND o.claimedByUserId IS NULL ORDER BY o.createdAt ASC LIMIT 1")
    Optional<OrderEntity> findNextPendingOrderFifo(@Param("outletId") Long outletId, @Param("statusId") Long statusId);

    List<OrderEntity> findAllByOutletIdOrderByCreatedAtDesc(Long outletId);
    List<OrderEntity> findAllByCustomerIdOrderByCreatedAtDesc(Long customerId);
}

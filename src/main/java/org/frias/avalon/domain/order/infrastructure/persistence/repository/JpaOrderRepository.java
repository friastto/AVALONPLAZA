package org.frias.avalon.domain.order.infrastructure.persistence.repository;

import org.frias.avalon.domain.order.infrastructure.persistence.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("omnichannelJpaOrderRepository")
public interface JpaOrderRepository extends JpaRepository<OrderEntity, Long> {
    Optional<OrderEntity> findByOrderCode(String orderCode);

    @Query("SELECT o FROM OmnichannelOrderEntity o WHERE o.outletId = :outletId AND o.orderStatusId = :statusId AND o.claimedByUserId IS NULL ORDER BY o.createdAt ASC LIMIT 1")
    Optional<OrderEntity> findNextPendingOrderFifo(@Param("outletId") Long outletId, @Param("statusId") Long statusId);

    List<OrderEntity> findAllByOutletIdOrderByCreatedAtDesc(Long outletId);
    List<OrderEntity> findAllByCustomerIdOrderByCreatedAtDesc(Long customerId);

    @Query("SELECT COALESCE(SUM(i.quantity), 0) FROM OmnichannelOrderItemEntity i JOIN OmnichannelOrderEntity o ON i.orderId = o.id WHERE i.productOutletId = :productOutletId AND o.orderStatusId IN :statusIds")
    Integer sumQuantityByProductOutletIdAndStatusIn(@Param("productOutletId") Long productOutletId, @Param("statusIds") List<Long> statusIds);

    @Query("SELECT COALESCE(SUM(i.quantity), 0) FROM OmnichannelOrderItemEntity i JOIN OmnichannelOrderEntity o ON i.orderId = o.id WHERE i.productOutletId = :productOutletId AND o.customerId = :customerId AND o.orderStatusId IN :statusIds")
    Integer sumQuantityByProductOutletIdAndCustomerIdAndStatusIn(@Param("productOutletId") Long productOutletId, @Param("customerId") Long customerId, @Param("statusIds") List<Long> statusIds);
}

package org.frias.avalon.domain.order.infrastructure.persistence.repository;

import org.frias.avalon.domain.order.infrastructure.persistence.entity.OrderItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaOrderItemRepository extends JpaRepository<OrderItemEntity, Long> {
    List<OrderItemEntity> findAllByOrderId(Long orderId);
}

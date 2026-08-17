package org.frias.avalon.domain.order.infrastructure.persistence.repository;

import org.frias.avalon.domain.order.infrastructure.persistence.entity.OrderStatusHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaOrderStatusHistoryRepository extends JpaRepository<OrderStatusHistoryEntity, Long> {
    List<OrderStatusHistoryEntity> findAllByOrderIdOrderByCreatedAtDesc(Long orderId);
}

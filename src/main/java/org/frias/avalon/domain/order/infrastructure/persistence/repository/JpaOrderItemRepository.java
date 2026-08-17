package org.frias.avalon.domain.order.infrastructure.persistence.repository;

import org.frias.avalon.domain.order.infrastructure.persistence.entity.OrderItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import org.springframework.stereotype.Repository;

@Repository("omnichannelJpaOrderItemRepository")
public interface JpaOrderItemRepository extends JpaRepository<OrderItemEntity, Long> {
    List<OrderItemEntity> findAllByOrderId(Long orderId);
}

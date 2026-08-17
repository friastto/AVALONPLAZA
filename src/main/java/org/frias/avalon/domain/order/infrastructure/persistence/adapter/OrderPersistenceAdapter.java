package org.frias.avalon.domain.order.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.frias.avalon.domain.order.application.port.OrderRepositoryPort;
import org.frias.avalon.domain.order.domain.OrderDomain;
import org.frias.avalon.domain.order.domain.OrderItemDomain;
import org.frias.avalon.domain.order.domain.OrderStatusHistoryDomain;
import org.frias.avalon.domain.order.infrastructure.persistence.entity.OrderEntity;
import org.frias.avalon.domain.order.infrastructure.persistence.entity.OrderItemEntity;
import org.frias.avalon.domain.order.infrastructure.persistence.entity.OrderStatusHistoryEntity;
import org.frias.avalon.domain.order.infrastructure.persistence.mapper.OrderMapper;
import org.frias.avalon.domain.order.infrastructure.persistence.repository.JpaOrderItemRepository;
import org.frias.avalon.domain.order.infrastructure.persistence.repository.JpaOrderRepository;
import org.frias.avalon.domain.order.infrastructure.persistence.repository.JpaOrderStatusHistoryRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component("omnichannelOrderPersistenceAdapter")
@RequiredArgsConstructor
public class OrderPersistenceAdapter implements OrderRepositoryPort {

    private final JpaOrderRepository jpaOrderRepository;
    private final JpaOrderItemRepository jpaOrderItemRepository;
    private final JpaOrderStatusHistoryRepository jpaOrderStatusHistoryRepository;
    private final @org.springframework.beans.factory.annotation.Qualifier("omnichannelOrderMapper") OrderMapper orderMapper;

    @Override
    public OrderDomain save(OrderDomain order) {
        OrderEntity entity = orderMapper.toEntity(order);
        OrderEntity savedEntity = jpaOrderRepository.save(entity);

        if (order.getItems() != null && !order.getItems().isEmpty()) {
            for (OrderItemDomain itemDomain : order.getItems()) {
                itemDomain.setOrderId(savedEntity.getId());
                OrderItemEntity itemEntity = orderMapper.toItemEntity(itemDomain);
                jpaOrderItemRepository.save(itemEntity);
            }
        }

        List<OrderItemEntity> itemEntities = jpaOrderItemRepository.findAllByOrderId(savedEntity.getId());
        return orderMapper.toDomain(savedEntity, itemEntities);
    }

    @Override
    public Optional<OrderDomain> findById(Long id) {
        return jpaOrderRepository.findById(id).map(entity -> {
            List<OrderItemEntity> items = jpaOrderItemRepository.findAllByOrderId(entity.getId());
            return orderMapper.toDomain(entity, items);
        });
    }

    @Override
    public Optional<OrderDomain> findByOrderCode(String orderCode) {
        return jpaOrderRepository.findByOrderCode(orderCode).map(entity -> {
            List<OrderItemEntity> items = jpaOrderItemRepository.findAllByOrderId(entity.getId());
            return orderMapper.toDomain(entity, items);
        });
    }

    @Override
    public Optional<OrderDomain> findNextPendingOrderFifo(Long outletId, Long statusId) {
        return jpaOrderRepository.findNextPendingOrderFifo(outletId, statusId).map(entity -> {
            List<OrderItemEntity> items = jpaOrderItemRepository.findAllByOrderId(entity.getId());
            return orderMapper.toDomain(entity, items);
        });
    }

    @Override
    public List<OrderDomain> findAllByOutletId(Long outletId) {
        return jpaOrderRepository.findAllByOutletIdOrderByCreatedAtDesc(outletId).stream().map(entity -> {
            List<OrderItemEntity> items = jpaOrderItemRepository.findAllByOrderId(entity.getId());
            return orderMapper.toDomain(entity, items);
        }).collect(Collectors.toList());
    }

    @Override
    public List<OrderDomain> findAllByCustomerId(Long customerId) {
        return jpaOrderRepository.findAllByCustomerIdOrderByCreatedAtDesc(customerId).stream().map(entity -> {
            List<OrderItemEntity> items = jpaOrderItemRepository.findAllByOrderId(entity.getId());
            return orderMapper.toDomain(entity, items);
        }).collect(Collectors.toList());
    }

    @Override
    public OrderItemDomain saveItem(OrderItemDomain item) {
        OrderItemEntity entity = orderMapper.toItemEntity(item);
        OrderItemEntity saved = jpaOrderItemRepository.save(entity);
        return orderMapper.toItemDomain(saved);
    }

    @Override
    public Optional<OrderItemDomain> findItemById(Long itemId) {
        return jpaOrderItemRepository.findById(itemId).map(orderMapper::toItemDomain);
    }

    @Override
    public OrderStatusHistoryDomain saveStatusHistory(OrderStatusHistoryDomain history) {
        OrderStatusHistoryEntity entity = orderMapper.toStatusHistoryEntity(history);
        OrderStatusHistoryEntity saved = jpaOrderStatusHistoryRepository.save(entity);
        return OrderStatusHistoryDomain.builder()
                .id(saved.getId())
                .orderId(saved.getOrderId())
                .previousStatusId(saved.getPreviousStatusId())
                .newStatusId(saved.getNewStatusId())
                .changedByUserId(saved.getChangedByUserId())
                .notes(saved.getNotes())
                .createdAt(saved.getCreatedAt())
                .build();
    }
}

package org.frias.avalon.domain.sale.infrastructure.adapter;

import lombok.RequiredArgsConstructor;
import org.frias.avalon.domain.sale.application.port.OrderRepositoryPort;
import org.frias.avalon.domain.sale.domain.OrderDomain;
import org.frias.avalon.domain.sale.infrastructure.entity.OrderEntity;
import org.frias.avalon.domain.sale.infrastructure.mapper.OrderMapper;
import org.frias.avalon.domain.sale.infrastructure.repository.JpaOrderRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderRepositoryAdapter implements OrderRepositoryPort {

    private final JpaOrderRepository jpaOrderRepository;
    private final OrderMapper orderMapper;

    @Override
    public OrderDomain save(OrderDomain order) {
        OrderEntity entity = orderMapper.toEntity(order);
        OrderEntity saved = jpaOrderRepository.save(entity);
        return orderMapper.toDomain(saved);
    }

    @Override
    public Optional<OrderDomain> findByCode(UUID code) {
        return jpaOrderRepository.findByOrderCode(code)
                .map(orderMapper::toDomain);
    }
}

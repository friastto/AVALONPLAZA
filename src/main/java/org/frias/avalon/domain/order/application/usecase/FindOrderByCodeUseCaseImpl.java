package org.frias.avalon.domain.order.application.usecase;

import lombok.RequiredArgsConstructor;
import org.frias.avalon.core.exeptions.ResourceNotFoundException;
import org.frias.avalon.domain.order.application.dto.OrderResponse;
import org.frias.avalon.domain.order.application.port.OrderRepositoryPort;
import org.frias.avalon.domain.order.domain.OrderDomain;
import org.frias.avalon.domain.order.infrastructure.persistence.mapper.OrderMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("omnichannelFindOrderByCodeUseCaseImpl")
@RequiredArgsConstructor
public class FindOrderByCodeUseCaseImpl implements FindOrderByCodeUseCase {

    private final OrderRepositoryPort orderRepositoryPort;
    private final @Qualifier("omnichannelOrderMapper") OrderMapper orderMapper;

    @Override
    @Transactional(readOnly = true)
    public OrderResponse execute(String orderCode) {
        OrderDomain order = orderRepositoryPort.findByOrderCode(orderCode)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido con codigo " + orderCode + " no encontrado"));
        return orderMapper.toResponse(order);
    }
}

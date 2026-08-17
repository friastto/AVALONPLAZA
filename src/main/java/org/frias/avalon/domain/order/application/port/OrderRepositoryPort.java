package org.frias.avalon.domain.order.application.port;

import org.frias.avalon.domain.order.domain.OrderDomain;
import org.frias.avalon.domain.order.domain.OrderItemDomain;
import org.frias.avalon.domain.order.domain.OrderStatusHistoryDomain;

import java.util.List;
import java.util.Optional;

public interface OrderRepositoryPort {
    OrderDomain save(OrderDomain order);
    Optional<OrderDomain> findById(Long id);
    Optional<OrderDomain> findByOrderCode(String orderCode);
    Optional<OrderDomain> findNextPendingOrderFifo(Long outletId, Long statusId);
    List<OrderDomain> findAllByOutletId(Long outletId);
    List<OrderDomain> findAllByCustomerId(Long customerId);
    OrderItemDomain saveItem(OrderItemDomain item);
    Optional<OrderItemDomain> findItemById(Long itemId);
    OrderStatusHistoryDomain saveStatusHistory(OrderStatusHistoryDomain history);
}

package org.frias.avalon.domain.sale.application.port;

import org.frias.avalon.domain.sale.domain.OrderDomain;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepositoryPort {

    OrderDomain save(OrderDomain order);

    Optional<OrderDomain> findByCode(UUID code);
}

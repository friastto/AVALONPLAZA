package org.frias.avalon.domain.claim.application.port;

import org.frias.avalon.domain.claim.domain.OrderClaimDomain;

import java.util.List;
import java.util.Optional;

public interface ClaimRepositoryPort {
    OrderClaimDomain save(OrderClaimDomain claim);
    Optional<OrderClaimDomain> findById(Long id);
    List<OrderClaimDomain> findAllByOrderId(Long orderId);
    List<OrderClaimDomain> findAllByCustomerId(Long customerId);
}

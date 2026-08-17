package org.frias.avalon.domain.order.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDomain {
    private Long id;
    private String orderCode;
    private Long customerId;
    private Long outletId;
    private Long orderStatusId;
    private Long paymentStatusId;
    private Long paymentMethodId;
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal total;
    private Long claimedByUserId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderItemDomain> items;
}

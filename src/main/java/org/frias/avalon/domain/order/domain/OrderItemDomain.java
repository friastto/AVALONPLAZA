package org.frias.avalon.domain.order.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDomain {
    private Long id;
    private Long orderId;
    private Long productOutletId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
    private Long dispatchStatusId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

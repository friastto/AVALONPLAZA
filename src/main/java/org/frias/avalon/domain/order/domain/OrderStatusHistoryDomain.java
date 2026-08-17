package org.frias.avalon.domain.order.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusHistoryDomain {
    private Long id;
    private Long orderId;
    private Long previousStatusId;
    private Long newStatusId;
    private Long changedByUserId;
    private String notes;
    private LocalDateTime createdAt;
}

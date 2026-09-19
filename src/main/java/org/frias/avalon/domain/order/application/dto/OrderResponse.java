package org.frias.avalon.domain.order.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.frias.avalon.domain.masterdata.application.dto.response.MasterRefDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private String orderCode;
    private Long customerId;
    private Long outletId;
    private String outletName;
    private Long orderStatusId;
    private String orderStatusCode;
    private MasterRefDto orderStatus;
    private Long paymentStatusId;
    private String paymentStatusCode;
    private MasterRefDto paymentStatus;
    private Long paymentMethodId;
    private MasterRefDto paymentMethod;
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal total;
    private Long claimedByUserId;
    private String claimedByName;
    private String claimedByUserName;
    private String deliveryType;
    private String deliveryAddress;
    private BigDecimal deliveryFee;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderItemResponse> items;
}

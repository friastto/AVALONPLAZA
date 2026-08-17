package org.frias.avalon.domain.order.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "omnichannel_orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_code", nullable = false, unique = true, length = 50)
    private String orderCode;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "outlet_id", nullable = false)
    private Long outletId;

    @Column(name = "order_status_id", nullable = false)
    private Long orderStatusId;

    @Column(name = "payment_status_id", nullable = false)
    private Long paymentStatusId;

    @Column(name = "payment_method_id")
    private Long paymentMethodId;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal tax;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal total;

    @Column(name = "claimed_by_user_id")
    private Long claimedByUserId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (updatedAt == null) updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

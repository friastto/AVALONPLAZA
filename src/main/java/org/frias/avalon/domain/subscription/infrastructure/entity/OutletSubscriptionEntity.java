package org.frias.avalon.domain.subscription.infrastructure.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA Entity mapping public.outlet_subscription table.
 */
@Entity
@Table(name = "outlet_subscription", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutletSubscriptionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "outlet_id", nullable = false, unique = true)
    private Long outletId;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "status_id", nullable = false)
    private Long statusId;

    @Column(name = "billing_day", nullable = false)
    private Integer billingDay;

    @Column(name = "amount_cop", precision = 12, scale = 2, nullable = false)
    private BigDecimal amountCop;

    @Column(name = "trial_ends_at", nullable = false)
    private LocalDateTime trialEndsAt;

    @Column(name = "current_period_start", nullable = false)
    private LocalDateTime currentPeriodStart;

    @Column(name = "current_period_end", nullable = false)
    private LocalDateTime currentPeriodEnd;

    @Column(name = "grace_period_end", nullable = false)
    private LocalDateTime gracePeriodEnd;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.billingDay == null) {
            this.billingDay = 28;
        }
        if (this.amountCop == null) {
            this.amountCop = new BigDecimal("60000.00");
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

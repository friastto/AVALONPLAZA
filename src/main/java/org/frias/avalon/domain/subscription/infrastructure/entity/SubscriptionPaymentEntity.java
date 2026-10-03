package org.frias.avalon.domain.subscription.infrastructure.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * JPA Entity mapping public.subscription_payment table.
 */
@Entity
@Table(name = "subscription_payment", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionPaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "outlet_subscription_id", nullable = false)
    private Long outletSubscriptionId;

    @Column(name = "outlet_id", nullable = false)
    private Long outletId;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "wompi_transaction_id", unique = true)
    private String wompiTransactionId;

    @Column(name = "wompi_reference", nullable = false, unique = true)
    private String wompiReference;

    @Column(name = "payment_method_type", length = 50)
    private String paymentMethodType;

    @Column(name = "amount_in_cents", nullable = false)
    private Long amountInCents;

    @Column(name = "currency", length = 10, nullable = false)
    private String currency;

    @Column(name = "status", length = 50, nullable = false)
    private String status;

    @Column(name = "checksum_sent", length = 150)
    private String checksumSent;

    @Column(name = "webhook_payload", columnDefinition = "TEXT")
    private String webhookPayload;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.currency == null) {
            this.currency = "COP";
        }
        if (this.status == null) {
            this.status = "PENDING";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

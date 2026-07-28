package org.frias.avalon.domain.cashregister.infrastructure.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cash_pickups")
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class CashPickupEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    private String reason;

    @Column(name = "pickup_time", nullable = false)
    private LocalDateTime pickupTime;

    @PrePersist
    protected void onCreate() {
        if (this.pickupTime == null) {
            this.pickupTime = LocalDateTime.now();
        }
    }
}

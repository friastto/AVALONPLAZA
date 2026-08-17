package org.frias.avalon.domain.claim.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_claim_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderClaimItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "claim_id", nullable = false)
    private Long claimId;

    @Column(name = "order_item_id", nullable = false)
    private Long orderItemId;

    @Column(name = "quantity_affected", nullable = false)
    private Integer quantityAffected;

    private String reason;
}

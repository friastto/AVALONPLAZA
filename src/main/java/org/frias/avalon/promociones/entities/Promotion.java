package org.frias.avalon.promociones.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.frias.avalon.Producto.entities.Product;
import org.frias.avalon.maestra.entities.MasterData;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "promoctions")
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnore
    private Product product;

    @Column(name = "discount", precision = 5, scale = 2, nullable = false)
    private BigDecimal discount; // Más corto que "discountPercentage"

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(length = 150)
    private String description;

    @ManyToOne
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_MasterData_promotypeid"))
    private MasterData promoTypeId;

    // --- Métodos auxiliares ---

    @Transient
    public boolean estaActiva() {
        LocalDate hoy = LocalDate.now();
        return (hoy.isEqual(startDate) || hoy.isAfter(startDate))
                && (hoy.isBefore(endDate) || hoy.isEqual(endDate));
    }


}

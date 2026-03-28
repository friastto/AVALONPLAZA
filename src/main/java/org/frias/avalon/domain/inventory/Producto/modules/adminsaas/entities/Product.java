package org.frias.avalon.domain.inventory.Producto.modules.adminsaas.entities;


import jakarta.persistence.*;
import lombok.*;
import org.frias.avalon.domain.masterdata.entities.MasterData;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "products")
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter

@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @ManyToOne
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_product_category"))
    private MasterData category;

    @ManyToOne
    @JoinColumn( foreignKey = @ForeignKey(name = "fk_product_unit"))
    private MasterData unit;

    private String imageUrl;

    @ManyToOne
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_product_status"))
    private MasterData status;

    // Relación para ver todos sus códigos asociados
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<ProductBarcode> barcodes;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
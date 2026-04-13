package org.frias.avalon.domain.inventory.Producto.modules.adminoulet.repository;

import jakarta.transaction.Transactional;
import org.frias.avalon.domain.product.domain.entity.ProductOutlet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductOutletRepository extends JpaRepository<ProductOutlet, Long> {
    @Query("""
            SELECT po FROM ProductOutlet po 
            JOIN po.companyProduct p 
            JOIN p.barcodes b 
            WHERE b.barcode = :scannedBarcode 
            AND po.outlet.id = :outletId 
            AND (b.company.id = po.outlet.company.id OR b.company IS NULL)
            """)
    Optional<ProductOutlet> findByBarcodeAndOutlet(@Param("scannedBarcode") String scannedBarcode,
                                                   @Param("outletId") Long outletId);

    // Busca la relación específica entre un producto y una sucursal
   @Query( """
SELECT po FROM ProductOutlet po  
            JOIN po.companyProduct pc  
            JOIN pc.product p  
            WHERE p.id = :productId AND po.outlet.id = :outletId
            """)
    Optional<ProductOutlet> findByProductIdAndOutletId(Long productId, Long outletId);

    @Query("""
            SELECT po FROM ProductOutlet po
            JOIN FETCH po.companyProduct pc
            JOIN FETCH pc.product p
            WHERE po.outlet.id = :outletId
           """)     // Filtrado por la sucursal actual

    List<ProductOutlet> findAllByOutletIdWithHierarchy(@Param("outletId") Long outletId);

    @Query("SELECT po FROM ProductOutlet po " +
            "JOIN FETCH po.outlet o " +
            "JOIN FETCH po.companyProduct cp " +
            "WHERE (cp.customName ILIKE %:nombre% OR po.localName ILIKE %:nombre%) " +
            "AND po.stock > 0 " +
            "AND po.active = true " +
            "AND (6371 * acos(cos(radians(:userLat)) * cos(radians(o.latitude)) * " +
            "cos(radians(o.longitude) - radians(:userLng)) + " +
            "sin(radians(:userLat)) * sin(radians(o.latitude)))) <= :radioKm")
    List<ProductOutlet> findByNameAndOutletRadius(
            @Param("name") String nombre,
            @Param("userLat") Double userLat,
            @Param("userLng") Double userLng,
            @Param("radioKm") int radioKm
    );

    @Modifying // <--- ESTA ES LA QUE FALTA
    @Transactional // Requerida para updates
    @Query("""
        UPDATE ProductOutlet p
        set p.localImageUrl = :finalFileName
        where p.id = :id
        """)
    void updateImageUrl(@Param("id") Long id,
                        @Param("finalFileName") String finalFileName);
}

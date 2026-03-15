package org.frias.avalon.Producto.modules.adminoulet.repository;

import org.frias.avalon.Producto.modules.adminsaas.entities.ProductOutlet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoOutletRepository extends JpaRepository<ProductOutlet, Long> {
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

}

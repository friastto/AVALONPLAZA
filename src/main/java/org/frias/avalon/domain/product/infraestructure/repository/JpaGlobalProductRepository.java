package org.frias.avalon.domain.product.infraestructure.repository;

import org.frias.avalon.domain.product.infraestructure.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio JPA para el Catalogo Global Maestro de Avalon (Nivel 1).
 */
@Repository
public interface JpaGlobalProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByBarcode(String barcode);

    @Query("SELECT p FROM Product p WHERE " +
           "(:statusId IS NULL OR p.statusId = :statusId) AND " +
           "(:query IS NULL OR :query = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "(p.barcode IS NOT NULL AND p.barcode LIKE CONCAT('%', :query, '%')))")
    Page<Product> searchGlobalProducts(@Param("statusId") Long statusId, @Param("query") String query, Pageable pageable);
}

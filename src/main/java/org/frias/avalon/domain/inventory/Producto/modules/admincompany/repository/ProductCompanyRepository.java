package org.frias.avalon.domain.inventory.Producto.modules.admincompany.repository;

import jakarta.transaction.Transactional;
import org.frias.avalon.domain.product.domain.entity.ProductCompany;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductCompanyRepository extends JpaRepository<ProductCompany, Long> {
    boolean existsByProductIdAndCompanyId(Long productId, Long companyId);

    @Modifying // <--- ESTA ES LA QUE FALTA
    @Transactional // Requerida para updates
    @Query("""
        UPDATE ProductCompany p
        set p.customImageUrl = :finalFileName
        where p.id = :id
        """)
    void updateImageUrl(@Param("id") Long id,
                        @Param("finalFileName") String finalFileName);
}

package org.frias.avalon.domain.product.infraestructure;

import jakarta.transaction.Transactional;
import org.frias.avalon.core.tenant.config.TenantAware;
import org.frias.avalon.domain.product.domain.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@TenantAware
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {


    Optional<Product> findByName(String sku);


    @Modifying // <--- ESTA ES LA QUE FALTA
    @Transactional // Requerida para updates
    @Query("""
        UPDATE Product p
        set p.imageUrl = :finalFileName
        where p.id = :id
        """)
    void updateImageUrl(@Param("id") Long id,
                        @Param("finalFileName") String finalFileName
    );

    //Long id(Long id);



    @Query("""
SELECT p 
FROM Product p 
WHERE LOWER(p.name) 
LIKE LOWER(CONCAT('%', :name, '%'))
""")
    List<Product> nearbyByName(@Param("name") String name);


}

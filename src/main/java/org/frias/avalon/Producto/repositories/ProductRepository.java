package org.frias.avalon.Producto.repositories;

import org.frias.avalon.Producto.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySku(String sku);


    Optional<Product> findByName(String sku);
}

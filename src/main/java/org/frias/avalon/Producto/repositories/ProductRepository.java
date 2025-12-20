package org.frias.avalon.Producto.repositories;

import org.frias.avalon.Producto.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}

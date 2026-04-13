package org.frias.avalon.domain.product.infraestructure.repository;

import org.frias.avalon.domain.product.domain.entity.Product;

import org.frias.avalon.domain.product.domain.repository.ProductRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class ProductRepositoryJpaAdapter implements ProductRepository{


   private final JpaProductRepository productRepository;

    public ProductRepositoryJpaAdapter(JpaProductRepository productRepository) {
        this.productRepository = productRepository;
    }


    public Product save(Product product) {

        return productRepository.save(product);
    }


    public Optional<Product> findById(Long aLong) {

        return productRepository.findById(aLong);
    }


    public void deleteById(Long aLong) {

        productRepository.deleteById(aLong);
    }
}

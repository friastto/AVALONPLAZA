package org.frias.avalon.domain.product.domain.repository;

import org.frias.avalon.domain.product.domain.entity.Product;

import java.util.List;
import java.util.Optional;

public interface CrudGenerics <T , E>{

    T save(T t);

    Optional<T> findById(E e);

    void deleteById(E e);
}

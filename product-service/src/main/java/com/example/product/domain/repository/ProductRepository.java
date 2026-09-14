package com.example.product.domain.repository;

import com.example.product.domain.model.Product;
import java.util.Optional;

/**
 * 리포지토리(Repository).
 * Aggregate Root(Product) 단위로만 조회/저장한다.
 */
public interface ProductRepository {
    Product save(Product product);

    Optional<Product> findById(Long id);
}

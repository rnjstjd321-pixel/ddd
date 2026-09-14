package com.example.product.infrastructure.persistence;

import com.example.product.domain.model.Product;
import com.example.product.domain.repository.ProductRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class ProductRepositoryAdapter implements ProductRepository {
    private final ProductJpaRepository jpaRepository;

    public ProductRepositoryAdapter(ProductJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Product save(Product product) {
        return jpaRepository.save(product);
    }

    @Override
    public Optional<Product> findById(Long id) {
        return jpaRepository.findById(id);
    }
}

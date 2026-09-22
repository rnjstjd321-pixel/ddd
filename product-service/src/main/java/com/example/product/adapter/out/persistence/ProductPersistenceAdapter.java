package com.example.product.adapter.out.persistence;

import com.example.product.domain.model.Product;
import com.example.product.application.port.out.ProductRepositoryPort;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class ProductPersistenceAdapter implements ProductRepositoryPort {
    private final ProductJpaRepository jpaRepository;

    public ProductPersistenceAdapter(ProductJpaRepository jpaRepository) {
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

    @Override
    public Optional<Product> findByIdForUpdate(Long id) {
        return jpaRepository.findByIdForUpdate(id);
    }
}

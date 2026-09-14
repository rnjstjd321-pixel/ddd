package com.example.product.infrastructure.persistence;

import com.example.product.domain.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductJpaRepository extends JpaRepository<Product, Long> {
}

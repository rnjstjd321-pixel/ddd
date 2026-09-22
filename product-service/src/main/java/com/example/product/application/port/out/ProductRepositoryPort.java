package com.example.product.application.port.out;

import com.example.product.domain.model.Product;
import java.util.Optional;

/**
 * Outbound Port.
 * Aggregate Root(Product) 단위로만 조회/저장한다.
 */
public interface ProductRepositoryPort {
    Product save(Product product);

    Optional<Product> findById(Long id);

    /** 재고 변경용 조회. 동시 요청에서 갱신이 유실되지 않도록 행 잠금을 잡는다. */
    Optional<Product> findByIdForUpdate(Long id);
}

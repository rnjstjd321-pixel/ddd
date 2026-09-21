package com.example.product.application.service;

import com.example.product.application.dto.CreateProductCommand;
import com.example.product.application.dto.ProductResponse;
import com.example.product.application.port.in.ProductUseCase;
import com.example.product.application.port.out.ProductRepositoryPort;
import com.example.product.domain.exception.ProductNotFoundException;
import com.example.product.domain.model.Product;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 애플리케이션 서비스.
 * Product Aggregate Root를 조회/저장만 하고, 재고 규칙은 product.decreaseStock()에 위임한다.
 */
@Service
@Transactional
public class ProductApplicationService implements ProductUseCase {
    private final ProductRepositoryPort productRepository;

    public ProductApplicationService(ProductRepositoryPort productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public ProductResponse createProduct(CreateProductCommand command) {
        Product product = Product.create(command.name(), command.price(), command.stock());
        return ProductResponse.from(productRepository.save(product));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProduct(Long productId) {
        return ProductResponse.from(findProduct(productId));
    }

    @Override
    public ProductResponse decreaseProductStock(Long productId, int quantity) {
        Product product = findProduct(productId);
        product.decreaseStock(quantity);
        return ProductResponse.from(productRepository.save(product));
    }

    @Override
    public ProductResponse increaseProductStock(Long productId, int quantity) {
        Product product = findProduct(productId);
        product.increaseStock(quantity);
        return ProductResponse.from(productRepository.save(product));
    }

    private Product findProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }
}

package com.example.product.application.service;

import com.example.product.application.dto.CreateProductCommand;
import com.example.product.application.dto.ProductResponse;
import com.example.product.application.dto.StockReservationResult;
import com.example.product.application.port.in.ProductUseCase;
import com.example.product.application.port.out.ProductRepositoryPort;
import com.example.product.application.port.out.StockReservationRepositoryPort;
import com.example.product.domain.exception.ProductNotFoundException;
import com.example.product.domain.model.Product;
import com.example.product.domain.model.StockReservation;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 애플리케이션 서비스.
 * Product Aggregate Root를 조회/저장만 하고, 재고 규칙은 product.decreaseStock()에 위임한다.
 * 재고 변경은 행 잠금으로 직렬화하고, 주문 단위 재고 처리는 StockReservation으로 멱등하게 만든다.
 */
@Service
@Transactional
public class ProductApplicationService implements ProductUseCase {
    private final ProductRepositoryPort productRepository;
    private final StockReservationRepositoryPort reservationRepository;

    public ProductApplicationService(
            ProductRepositoryPort productRepository,
            StockReservationRepositoryPort reservationRepository) {
        this.productRepository = productRepository;
        this.reservationRepository = reservationRepository;
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
        Product product = findProductForUpdate(productId);
        product.decreaseStock(quantity);
        return ProductResponse.from(productRepository.save(product));
    }

    @Override
    public ProductResponse increaseProductStock(Long productId, int quantity) {
        Product product = findProductForUpdate(productId);
        product.increaseStock(quantity);
        return ProductResponse.from(productRepository.save(product));
    }

    @Override
    public StockReservationResult reserveStock(Long orderId, Long productId, int quantity) {
        Optional<StockReservation> existing = reservationRepository.findByOrderId(orderId);
        if (existing.isPresent()) {
            return existing.get().isDeducted()
                    ? StockReservationResult.ALREADY_RESERVED
                    : StockReservationResult.ALREADY_RELEASED;
        }
        Product product = findProductForUpdate(productId);
        product.decreaseStock(quantity);
        productRepository.save(product);
        reservationRepository.save(StockReservation.deducted(orderId, productId, quantity));
        return StockReservationResult.RESERVED;
    }

    @Override
    public void releaseStock(Long orderId, Long productId, int quantity) {
        Optional<StockReservation> existing = reservationRepository.findByOrderId(orderId);
        if (existing.isEmpty()) {
            reservationRepository.save(StockReservation.cancelledBeforeDeduct(orderId, productId, quantity));
            return;
        }
        StockReservation reservation = existing.get();
        if (!reservation.isDeducted()) {
            return;
        }
        Product product = findProductForUpdate(reservation.getProductId());
        product.increaseStock(reservation.getQuantity());
        productRepository.save(product);
        reservation.release();
        reservationRepository.save(reservation);
    }

    private Product findProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }

    private Product findProductForUpdate(Long productId) {
        return productRepository.findByIdForUpdate(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }
}

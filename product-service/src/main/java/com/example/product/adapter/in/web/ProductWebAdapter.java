package com.example.product.adapter.in.web;

import com.example.product.application.dto.ProductResponse;
import com.example.product.application.port.in.ProductUseCase;
import com.example.product.adapter.in.web.dto.CreateProductRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Inbound Adapter (HTTP). 웹 요청을 Inbound Port(ProductUseCase)로 전달한다.
 */
@RestController
@RequestMapping("/api/products")
public class ProductWebAdapter {
    private final ProductUseCase productUseCase;

    public ProductWebAdapter(ProductUseCase productUseCase) {
        this.productUseCase = productUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse createProduct(@RequestBody CreateProductRequest request) {
        return productUseCase.createProduct(request.toCommand());
    }

    @GetMapping("/{id}")
    public ProductResponse getProduct(@PathVariable Long id) {
        return productUseCase.getProduct(id);
    }

    @PostMapping("/{id}/decrease-stock")
    public ProductResponse decreaseProductStock(@PathVariable Long id, @RequestParam int quantity) {
        return productUseCase.decreaseProductStock(id, quantity);
    }

    @PostMapping("/{id}/increase-stock")
    public ProductResponse increaseProductStock(@PathVariable Long id, @RequestParam int quantity) {
        return productUseCase.increaseProductStock(id, quantity);
    }
}

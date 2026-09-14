package com.example.product.presentation;

import com.example.product.application.dto.ProductResponse;
import com.example.product.application.service.ProductApplicationService;
import com.example.product.presentation.dto.CreateProductRequest;
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
 * HTTP 진입점. 도메인 객체를 직접 다루지 않고 Application Service에 유스케이스를 위임한다.
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductApplicationService productApplicationService;

    public ProductController(ProductApplicationService productApplicationService) {
        this.productApplicationService = productApplicationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse createProduct(@RequestBody CreateProductRequest request) {
        return productApplicationService.createProduct(request.toCommand());
    }

    @GetMapping("/{id}")
    public ProductResponse getProduct(@PathVariable Long id) {
        return productApplicationService.getProduct(id);
    }

    @PostMapping("/{id}/decrease-stock")
    public ProductResponse decreaseProductStock(@PathVariable Long id, @RequestParam int quantity) {
        return productApplicationService.decreaseProductStock(id, quantity);
    }

    @PostMapping("/{id}/increase-stock")
    public ProductResponse increaseProductStock(@PathVariable Long id, @RequestParam int quantity) {
        return productApplicationService.increaseProductStock(id, quantity);
    }
}

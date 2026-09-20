package com.example.order.infrastructure.client;

import com.example.order.application.exception.ProductUnavailableException;
import com.example.order.application.port.ProductCatalogPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class ProductCatalogAdapter implements ProductCatalogPort {
    private final RestClient restClient;
    private final String productUrl;

    public ProductCatalogAdapter(
            RestClient restClient,
            @Value("${services.product.url}") String productUrl) {
        this.restClient = restClient;
        this.productUrl = productUrl;
    }

    @Override
    public ProductInfo getProduct(Long productId) {
        try {
            ProductInfo product = restClient.get()
                    .uri(productUrl + "/api/products/" + productId)
                    .retrieve()
                    .body(ProductInfo.class);
            if (product == null) {
                throw new ProductUnavailableException(productId);
            }
            return product;
        } catch (RestClientException e) {
            throw new ProductUnavailableException(productId);
        }
    }
}

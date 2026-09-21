package com.example.order.config;

import com.example.order.domain.service.PlaceOrderService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 프레임워크에 의존하지 않는 도메인 서비스를 Bean으로 등록한다.
 */
@Configuration
public class DomainConfig {

    @Bean
    PlaceOrderService placeOrderService() {
        return new PlaceOrderService();
    }
}

package com.example.payment.presentation;

import com.example.payment.application.dto.PaymentResponse;
import com.example.payment.application.service.PaymentApplicationService;
import com.example.payment.presentation.dto.ApprovePaymentRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * HTTP 진입점. 도메인 객체를 직접 다루지 않고 Application Service에 유스케이스를 위임한다.
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentApplicationService paymentApplicationService;

    public PaymentController(PaymentApplicationService paymentApplicationService) {
        this.paymentApplicationService = paymentApplicationService;
    }

    @PostMapping("/approve")
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse createPayment(@RequestBody ApprovePaymentRequest request) {
        return paymentApplicationService.createPayment(request.toCommand());
    }

    @GetMapping("/{id}")
    public PaymentResponse getPayment(@PathVariable Long id) {
        return paymentApplicationService.getPayment(id);
    }

    @PostMapping("/order/{orderId}/cancel")
    public PaymentResponse cancelPayment(@PathVariable Long orderId) {
        return paymentApplicationService.cancelPayment(orderId);
    }
}

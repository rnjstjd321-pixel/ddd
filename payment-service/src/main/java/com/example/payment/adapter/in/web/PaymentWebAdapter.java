package com.example.payment.adapter.in.web;

import com.example.payment.adapter.in.web.dto.ApprovePaymentRequest;
import com.example.payment.application.dto.PaymentResponse;
import com.example.payment.application.port.in.PaymentUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Inbound Adapter (HTTP).
 * 웹 요청을 Inbound Port(PaymentUseCase)로 전달한다.
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentWebAdapter {
    private final PaymentUseCase paymentUseCase;

    public PaymentWebAdapter(PaymentUseCase paymentUseCase) {
        this.paymentUseCase = paymentUseCase;
    }

    @PostMapping("/approve")
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse createPayment(@RequestBody ApprovePaymentRequest request) {
        return paymentUseCase.createPayment(request.toCommand());
    }

    @GetMapping("/{id}")
    public PaymentResponse getPayment(@PathVariable Long id) {
        return paymentUseCase.getPayment(id);
    }

    @PostMapping("/order/{orderId}/cancel")
    public PaymentResponse cancelPayment(@PathVariable Long orderId) {
        return paymentUseCase.cancelPayment(orderId);
    }
}

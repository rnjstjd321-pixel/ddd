package com.example.order.infrastructure.client;

import com.example.order.application.exception.PaymentApprovalFailedException;
import com.example.order.application.port.PaymentGatewayPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class PaymentGatewayAdapter implements PaymentGatewayPort {
    private final RestClient restClient;
    private final String paymentUrl;

    public PaymentGatewayAdapter(
            RestClient restClient,
            @Value("${services.payment.url}") String paymentUrl) {
        this.restClient = restClient;
        this.paymentUrl = paymentUrl;
    }

    @Override
    public PaymentResult approve(Long orderId, long amount) {
        try {
            PaymentResult payment = restClient.post()
                    .uri(paymentUrl + "/api/payments/approve")
                    .body(new ApprovePaymentRequest(orderId, amount))
                    .retrieve()
                    .body(PaymentResult.class);
            if (payment == null) {
                throw new PaymentApprovalFailedException();
            }
            return payment;
        } catch (RestClientException e) {
            throw new PaymentApprovalFailedException();
        }
    }

    @Override
    public void cancelByOrderId(Long orderId) {
        restClient.post()
                .uri(paymentUrl + "/api/payments/order/" + orderId + "/cancel")
                .retrieve()
                .toBodilessEntity();
    }

    private record ApprovePaymentRequest(Long orderId, long amount) {
    }
}

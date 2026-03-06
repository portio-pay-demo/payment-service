package io.portioapay.payment.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.portioapay.payment.model.PaymentRequest;
import io.portioapay.payment.model.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Wraps the external payment gateway with circuit breaker and retry logic.
 *
 * NP-2041: Added exponential backoff (max 3 retries, 500ms/1s/2s) and circuit
 * breaker (opens at 50% failure rate over 10 calls, 30s wait).
 */
@Slf4j
@Service
public class PaymentGatewayService {

    private static final String GATEWAY_CB = "paymentGateway";

    @CircuitBreaker(name = GATEWAY_CB, fallbackMethod = "gatewayFallback")
    @Retry(name = GATEWAY_CB)
    public String authorize(PaymentRequest request) {
        log.info("Authorizing payment for merchant={} idempotencyKey={}",
                request.getMerchantId(), request.getIdempotencyKey());
        return callGatewayAuthorize(request);
    }

    @CircuitBreaker(name = GATEWAY_CB, fallbackMethod = "gatewayFallback")
    @Retry(name = GATEWAY_CB)
    public String capture(String gatewayTransactionId, java.math.BigDecimal amount) {
        log.info("Capturing gateway transaction={} amount={}", gatewayTransactionId, amount);
        return callGatewayCapture(gatewayTransactionId, amount);
    }

    private String gatewayFallback(PaymentRequest request, Exception ex) {
        log.error("Gateway circuit open or retries exhausted for idempotencyKey={}. Error: {}",
                request.getIdempotencyKey(), ex.getMessage());
        throw new RuntimeException("Payment gateway unavailable. Please retry.", ex);
    }

    private String gatewayFallback(String gatewayTransactionId, java.math.BigDecimal amount, Exception ex) {
        log.error("Gateway capture failed for txn={}. Error: {}", gatewayTransactionId, ex.getMessage());
        throw new RuntimeException("Payment capture failed. Please retry.", ex);
    }

    // Simulates gateway HTTP call — replaced with actual provider SDK in production
    private String callGatewayAuthorize(PaymentRequest request) {
        return "gtw_" + request.getIdempotencyKey();
    }

    private String callGatewayCapture(String gatewayTransactionId, java.math.BigDecimal amount) {
        return gatewayTransactionId + "_captured";
    }
}

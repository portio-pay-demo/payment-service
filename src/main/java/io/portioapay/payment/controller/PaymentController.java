package io.portioapay.payment.controller;

import io.portioapay.payment.model.PaymentRequest;
import io.portioapay.payment.model.Transaction;
import io.portioapay.payment.service.AuditLogService;
import io.portioapay.payment.service.PaymentGatewayService;
import io.portioapay.payment.service.RateLimiterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentGatewayService gatewayService;
    private final RateLimiterService rateLimiterService;
    private final AuditLogService auditLogService;

    @PostMapping("/authorize")
    public ResponseEntity<?> authorize(
            @Valid @RequestBody PaymentRequest request,
            @RequestHeader("X-Merchant-Tier") String merchantTier) {

        if (!rateLimiterService.isAllowed(request.getMerchantId(), merchantTier)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("error", "rate_limit_exceeded",
                            "message", "Too many requests. Retry after 1 second."));
        }

        String gatewayTxnId = gatewayService.authorize(request);

        auditLogService.logPaymentEvent(
                "PAYMENT_AUTHORIZED", request.getIdempotencyKey(),
                request.getMerchantId(), null, "success",
                Map.of("amount", request.getAmount(), "currency", request.getCurrency())
        );

        return ResponseEntity.ok(Map.of(
                "transactionId", request.getIdempotencyKey(),
                "gatewayTransactionId", gatewayTxnId,
                "status", "AUTHORIZED"
        ));
    }

    @PostMapping("/capture")
    public ResponseEntity<?> capture(
            @RequestParam String gatewayTransactionId,
            @RequestParam java.math.BigDecimal amount,
            @RequestHeader("X-Merchant-Id") String merchantId) {

        String captureId = gatewayService.capture(gatewayTransactionId, amount);

        auditLogService.logPaymentEvent(
                "PAYMENT_CAPTURED", gatewayTransactionId,
                merchantId, null, "success",
                Map.of("amount", amount, "captureId", captureId)
        );

        return ResponseEntity.ok(Map.of(
                "captureId", captureId,
                "status", "CAPTURED"
        ));
    }
}

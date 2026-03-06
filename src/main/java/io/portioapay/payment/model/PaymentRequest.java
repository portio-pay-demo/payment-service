package io.portioapay.payment.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentRequest {

    @NotNull
    private String merchantId;

    @NotNull
    private String idempotencyKey;

    @NotNull
    @Positive
    private BigDecimal amount;

    @NotNull
    private String currency;

    @NotNull
    private String paymentMethodToken;

    private String customerId;
    private String description;
    private String metadata;
}

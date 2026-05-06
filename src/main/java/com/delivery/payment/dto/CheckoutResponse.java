package com.delivery.payment.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
public class CheckoutResponse {
    private UUID transactionId;
    private String paymentIntentId;
    private String clientSecret;
    private BigDecimal amount;
    private String currency;
}

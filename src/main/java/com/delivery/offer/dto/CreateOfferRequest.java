package com.delivery.offer.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class CreateOfferRequest {
    private String title;
    private int discountPercent;
    private BigDecimal minOrderAmount;
    private String description;
    private LocalDateTime expiresAt;
}

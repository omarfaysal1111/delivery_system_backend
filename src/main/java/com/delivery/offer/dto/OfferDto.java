package com.delivery.offer.dto;

import com.delivery.offer.domain.Offer;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class OfferDto {
    private UUID id;
    private UUID restaurantId;
    private String title;
    private int discountPercent;
    private BigDecimal minOrderAmount;
    private String description;
    private LocalDateTime expiresAt;

    public static OfferDto from(Offer o) {
        return OfferDto.builder()
                .id(o.getId()).restaurantId(o.getRestaurantId())
                .title(o.getTitle()).discountPercent(o.getDiscountPercent())
                .minOrderAmount(o.getMinOrderAmount()).description(o.getDescription())
                .expiresAt(o.getExpiresAt())
                .build();
    }
}

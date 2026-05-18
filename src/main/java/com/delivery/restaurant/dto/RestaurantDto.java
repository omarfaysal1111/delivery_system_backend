package com.delivery.restaurant.dto;

import com.delivery.restaurant.domain.Restaurant;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
public class RestaurantDto {
    private UUID id;
    private UUID ownerId;
    private String name;
    private String slug;
    private String cuisineType;
    private String coverImageUrl;
    private String logoImageUrl;
    private String description;
    private Integer deliveryTimeMin;
    private Integer deliveryTimeMax;
    private BigDecimal deliveryFee;
    private BigDecimal minimumOrder;
    private BigDecimal commissionPct;
    private boolean isActive;
    private Double rating;
    private Long ratingCount;
    private boolean isOpenNow;

    public static RestaurantDto from(Restaurant r) {
        return from(r, 0.0, 0L, false);
    }

    public static RestaurantDto from(Restaurant r, Double rating, Long ratingCount, boolean isOpenNow) {
        return RestaurantDto.builder()
                .id(r.getId()).ownerId(r.getOwnerId())
                .name(r.getName()).slug(r.getSlug())
                .cuisineType(r.getCuisineType())
                .coverImageUrl(r.getCoverImageUrl())
                .logoImageUrl(r.getLogoImageUrl())
                .description(r.getDescription())
                .deliveryTimeMin(r.getDeliveryTimeMin())
                .deliveryTimeMax(r.getDeliveryTimeMax())
                .deliveryFee(r.getDeliveryFee())
                .minimumOrder(r.getMinimumOrder())
                .commissionPct(r.getCommissionPct())
                .isActive(r.isActive())
                .rating(rating)
                .ratingCount(ratingCount)
                .isOpenNow(isOpenNow)
                .build();
    }
}

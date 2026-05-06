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
    private BigDecimal commissionPct;
    private boolean isActive;

    public static RestaurantDto from(Restaurant r) {
        return RestaurantDto.builder()
                .id(r.getId()).ownerId(r.getOwnerId())
                .name(r.getName()).slug(r.getSlug())
                .cuisineType(r.getCuisineType())
                .commissionPct(r.getCommissionPct())
                .isActive(r.isActive())
                .build();
    }
}

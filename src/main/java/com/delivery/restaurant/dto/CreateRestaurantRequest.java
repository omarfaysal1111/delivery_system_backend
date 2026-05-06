package com.delivery.restaurant.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateRestaurantRequest {
    @NotBlank private String name;
    @NotBlank private String slug;
    private String cuisineType;
    private BigDecimal commissionPct;
}

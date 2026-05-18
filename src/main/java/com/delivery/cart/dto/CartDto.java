package com.delivery.cart.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class CartDto {
    private UUID id;
    private UUID restaurantId;
    private String restaurantName;
    private List<CartItemDto> items;
    private BigDecimal subtotal;
    private BigDecimal deliveryFee;
    private BigDecimal discount;
    private BigDecimal total;
}

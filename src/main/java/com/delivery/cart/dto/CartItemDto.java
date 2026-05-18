package com.delivery.cart.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Builder
public class CartItemDto {
    private UUID id;
    private UUID menuItemId;
    private String itemName;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private List<Map<String, Object>> selectedModifiers;
}

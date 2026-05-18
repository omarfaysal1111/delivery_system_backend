package com.delivery.cart.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class AddCartItemRequest {
    private UUID menuItemId;
    private int quantity;
    private List<Map<String, Object>> selectedModifiers;
}

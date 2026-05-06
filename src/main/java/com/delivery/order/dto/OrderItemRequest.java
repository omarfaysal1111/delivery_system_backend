package com.delivery.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class OrderItemRequest {
    @NotNull private UUID menuItemId;
    @Min(1)  private int qty;
    private List<Map<String, Object>> modifiers;
}

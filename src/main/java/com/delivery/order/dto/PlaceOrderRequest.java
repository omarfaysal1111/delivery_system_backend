package com.delivery.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class PlaceOrderRequest {
    @NotNull  private UUID branchId;
    @NotEmpty @Valid private List<OrderItemRequest> items;
    @NotNull  private BigDecimal deliveryFee;

    @NotBlank private String deliveryAddress;
    @NotNull  private Double deliveryLat;
    @NotNull  private Double deliveryLng;

    private String paymentMethod;
    private String promoCode;
    private String specialInstructions;
}

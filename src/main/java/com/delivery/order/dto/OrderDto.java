package com.delivery.order.dto;

import com.delivery.order.domain.Order;
import com.delivery.order.domain.OrderStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class OrderDto {
    private UUID id;
    private UUID customerId;
    private UUID branchId;
    private OrderStatus status;
    private BigDecimal subtotal;
    private BigDecimal deliveryFee;
    private BigDecimal total;
    private BigDecimal discount;
    private List<OrderItemDto> items;
    private LocalDateTime createdAt;

    private String restaurantName;
    private String deliveryAddress;
    private Double deliveryLat;
    private Double deliveryLng;
    private String paymentMethod;
    private Integer estimatedMinutes;

    private String driverName;
    private String driverPhone;
    private Double driverLat;
    private Double driverLng;

    @Getter
    @Builder
    public static class OrderItemDto {
        private UUID id;
        private UUID menuItemId;
        private String itemName;
        private int qty;
        private BigDecimal unitPrice;
    }

    public static OrderDto from(Order o) {
        List<OrderItemDto> itemDtos = o.getItems().stream()
                .map(i -> OrderItemDto.builder()
                        .id(i.getId()).menuItemId(i.getMenuItemId())
                        .itemName(i.getItemName()).qty(i.getQty())
                        .unitPrice(i.getUnitPrice()).build())
                .toList();
        return OrderDto.builder()
                .id(o.getId()).customerId(o.getCustomerId()).branchId(o.getBranchId())
                .status(o.getStatus()).subtotal(o.getSubtotal())
                .deliveryFee(o.getDeliveryFee()).total(o.getTotal())
                .discount(o.getDiscount())
                .deliveryAddress(o.getDeliveryAddress())
                .deliveryLat(o.getDeliveryLat())
                .deliveryLng(o.getDeliveryLng())
                .paymentMethod(o.getPaymentMethod())
                .items(itemDtos).createdAt(o.getCreatedAt())
                .build();
    }
}

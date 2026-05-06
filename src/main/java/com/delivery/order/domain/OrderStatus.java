package com.delivery.order.domain;

public enum OrderStatus {
    PENDING,
    CONFIRMED,
    PREPARING,
    READY_FOR_PICKUP,
    PICKED_UP,
    DELIVERED,
    CANCELLED
}

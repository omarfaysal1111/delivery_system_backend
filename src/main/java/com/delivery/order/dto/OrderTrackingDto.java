package com.delivery.order.dto;

import com.delivery.order.domain.OrderStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class OrderTrackingDto {
    private UUID orderId;
    private OrderStatus status;
    private Integer estimatedMinutes;
    private String driverName;
    private String driverPhone;
    private Double driverLat;
    private Double driverLng;
    private List<TimelineEntry> timeline;

    @Getter
    @Builder
    public static class TimelineEntry {
        private OrderStatus status;
        private LocalDateTime timestamp;
    }
}

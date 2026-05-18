package com.delivery.order.service;

import com.delivery.common.exception.ResourceNotFoundException;
import com.delivery.delivery.repository.DeliveryRepository;
import com.delivery.order.domain.Order;
import com.delivery.order.dto.OrderTrackingDto;
import com.delivery.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderTrackingService {

    private final OrderService orderService;
    private final DeliveryRepository deliveryRepository;
    private final UserRepository userRepository;

    public OrderTrackingDto getTracking(UUID orderId) {
        Order order = orderService.findOrder(orderId);

        var trackingBuilder = OrderTrackingDto.builder()
                .orderId(orderId)
                .status(order.getStatus())
                .timeline(List.of(
                        OrderTrackingDto.TimelineEntry.builder()
                                .status(order.getStatus())
                                .timestamp(order.getCreatedAt())
                                .build()
                ));

        deliveryRepository.findByOrderId(orderId).ifPresent(delivery -> {
            trackingBuilder.driverLat(delivery.getCurrentLat());
            trackingBuilder.driverLng(delivery.getCurrentLng());

            if (delivery.getDriverId() != null) {
                userRepository.findById(delivery.getDriverId()).ifPresent(driver -> {
                    trackingBuilder.driverName(driver.getName());
                    trackingBuilder.driverPhone(driver.getPhone());
                });
            }
        });

        return trackingBuilder.build();
    }
}

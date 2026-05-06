package com.delivery.order.service;

import com.delivery.order.domain.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderStatusNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public void notifyStatusChange(UUID orderId, OrderStatus status) {
        messagingTemplate.convertAndSend(
                "/topic/orders/" + orderId + "/status",
                Map.of("orderId", orderId.toString(), "status", status.name())
        );
    }
}

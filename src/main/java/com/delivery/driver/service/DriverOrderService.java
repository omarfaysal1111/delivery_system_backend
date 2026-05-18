package com.delivery.driver.service;

import com.delivery.common.exception.AuthException;
import com.delivery.common.exception.ResourceNotFoundException;
import com.delivery.delivery.domain.Delivery;
import com.delivery.delivery.domain.DeliveryStatus;
import com.delivery.delivery.repository.DeliveryRepository;
import com.delivery.driver.domain.DriverEarnings;
import com.delivery.driver.repository.DriverEarningsRepository;
import com.delivery.order.domain.Order;
import com.delivery.order.domain.OrderStatus;
import com.delivery.order.dto.OrderDto;
import com.delivery.order.service.OrderService;
import com.delivery.order.service.OrderStatusNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DriverOrderService {

    private final DeliveryRepository deliveryRepository;
    private final OrderService orderService;
    private final OrderStatusNotificationService notificationService;
    private final DriverEarningsRepository earningsRepository;
    private final ApplicationEventPublisher eventPublisher;

    private static final List<DeliveryStatus> ACTIVE_STATUSES = List.of(
            DeliveryStatus.ASSIGNED,
            DeliveryStatus.EN_ROUTE_TO_RESTAURANT,
            DeliveryStatus.PICKED_UP
    );

    public OrderDto getActiveOrder(UUID driverId) {
        return deliveryRepository.findByDriverIdAndStatusIn(driverId, ACTIVE_STATUSES)
                .stream().findFirst()
                .map(d -> orderService.getOrder(d.getOrderId()))
                .orElse(null);
    }

    @Transactional
    public OrderDto acceptOrder(UUID driverId, UUID orderId) {
        Delivery delivery = findDeliveryForDriver(driverId, orderId);
        delivery.setStatus(DeliveryStatus.EN_ROUTE_TO_RESTAURANT);
        deliveryRepository.save(delivery);
        notificationService.notifyStatusChange(orderId, OrderStatus.CONFIRMED);
        return orderService.getOrder(orderId);
    }

    @Transactional
    public void declineOrder(UUID driverId, UUID orderId) {
        Delivery delivery = findDeliveryForDriver(driverId, orderId);
        delivery.setDriverId(null);
        delivery.setStatus(DeliveryStatus.ASSIGNED);
        deliveryRepository.save(delivery);
        eventPublisher.publishEvent(new OrderDeclinedEvent(this, orderId));
    }

    @Transactional
    public void arrivedAtRestaurant(UUID driverId, UUID orderId) {
        Delivery delivery = findDeliveryForDriver(driverId, orderId);
        delivery.setStatus(DeliveryStatus.EN_ROUTE_TO_RESTAURANT);
        deliveryRepository.save(delivery);
        notificationService.notifyStatusChange(orderId, OrderStatus.READY_FOR_PICKUP);
    }

    @Transactional
    public void confirmDelivery(UUID driverId, UUID orderId, MultipartFile photo) {
        Delivery delivery = findDeliveryForDriver(driverId, orderId);
        Order order = orderService.findOrder(orderId);

        if (photo != null && !photo.isEmpty()) {
            try {
                Path uploadDir = Paths.get("uploads");
                Files.createDirectories(uploadDir);
                Path dest = uploadDir.resolve(orderId + "-proof" + getExtension(photo));
                Files.write(dest, photo.getBytes());
                log.info("[DELIVERY PROOF] saved to {}", dest);
            } catch (IOException e) {
                log.warn("[DELIVERY PROOF] could not save photo: {}", e.getMessage());
            }
        }

        delivery.setStatus(DeliveryStatus.DELIVERED);
        delivery.setDeliveredTime(java.time.LocalDateTime.now());
        deliveryRepository.save(delivery);
        orderService.updateStatus(orderId, OrderStatus.DELIVERED);

        if (order.getDeliveryFee() != null) {
            earningsRepository.save(DriverEarnings.builder()
                    .driverId(driverId)
                    .orderId(orderId)
                    .type("trip")
                    .amount(order.getDeliveryFee())
                    .description("Delivery fee for order " + orderId)
                    .build());
        }
    }

    @Transactional
    public void reportIssue(UUID driverId, UUID orderId, Map<String, String> body) {
        log.warn("[ORDER ISSUE] driver={} order={} type={} desc={}",
                driverId, orderId, body.get("type"), body.get("description"));
        orderService.updateStatus(orderId, OrderStatus.CANCELLED);
    }

    private Delivery findDeliveryForDriver(UUID driverId, UUID orderId) {
        Delivery delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found for order: " + orderId));
        if (delivery.getDriverId() != null && !delivery.getDriverId().equals(driverId)) {
            throw new AuthException("Not authorized for this delivery");
        }
        return delivery;
    }

    private String getExtension(MultipartFile file) {
        String name = file.getOriginalFilename();
        if (name != null && name.contains(".")) {
            return name.substring(name.lastIndexOf('.'));
        }
        return ".jpg";
    }

    public static class OrderDeclinedEvent extends org.springframework.context.ApplicationEvent {
        private final UUID orderId;

        public OrderDeclinedEvent(Object source, UUID orderId) {
            super(source);
            this.orderId = orderId;
        }

        public UUID getOrderId() { return orderId; }
    }
}

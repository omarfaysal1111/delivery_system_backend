package com.delivery.delivery.service;

import com.delivery.delivery.domain.Delivery;
import com.delivery.delivery.domain.DeliveryStatus;
import com.delivery.delivery.dto.LocationUpdateRequest;
import com.delivery.delivery.repository.DeliveryRepository;
import com.delivery.order.domain.OrderStatus;
import com.delivery.order.service.OrderService;
import com.delivery.user.domain.DriverProfile;
import com.delivery.user.repository.DriverProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final DriverProfileRepository driverProfileRepository;
    private final OrderService orderService;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String GEO_KEY = "drivers:geo";

    @Transactional
    public Delivery assignDriver(UUID orderId, UUID driverId) {
        Delivery delivery = Delivery.builder()
                .orderId(orderId)
                .driverId(driverId)
                .status(DeliveryStatus.ASSIGNED)
                .build();
        Delivery saved = deliveryRepository.save(delivery);
        orderService.updateStatus(orderId, OrderStatus.CONFIRMED);
        return saved;
    }

    @Transactional
    public void updateLocation(UUID deliveryId, UUID driverId, LocationUpdateRequest req) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found: " + deliveryId));

        delivery.setCurrentLat(req.getLat());
        delivery.setCurrentLng(req.getLng());
        deliveryRepository.save(delivery);

        // Update driver's last known position in their profile
        driverProfileRepository.findByUserId(driverId).ifPresent(dp -> {
            dp.setLastLat(req.getLat());
            dp.setLastLng(req.getLng());
            driverProfileRepository.save(dp);
        });

        // Update Redis geo set for real-time driver availability map
        redisTemplate.opsForGeo().add(GEO_KEY,
                new Point(req.getLng(), req.getLat()), driverId.toString());
    }

    @Transactional
    public void markPickedUp(UUID deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found"));
        delivery.setStatus(DeliveryStatus.PICKED_UP);
        delivery.setPickupTime(LocalDateTime.now());
        deliveryRepository.save(delivery);
        orderService.updateStatus(delivery.getOrderId(), OrderStatus.PICKED_UP);
    }

    @Transactional
    public void markDelivered(UUID deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found"));
        delivery.setStatus(DeliveryStatus.DELIVERED);
        delivery.setDeliveredTime(LocalDateTime.now());
        deliveryRepository.save(delivery);
        orderService.updateStatus(delivery.getOrderId(), OrderStatus.DELIVERED);
    }

    public void setDriverOnline(UUID userId, boolean online) {
        driverProfileRepository.findByUserId(userId).ifPresent(dp -> {
            dp.setOnline(online);
            driverProfileRepository.save(dp);
            if (!online) {
                redisTemplate.opsForGeo().remove(GEO_KEY, userId.toString());
            }
        });
    }
}

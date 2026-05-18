package com.delivery.delivery.repository;

import com.delivery.delivery.domain.Delivery;
import com.delivery.delivery.domain.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeliveryRepository extends JpaRepository<Delivery, UUID> {
    Optional<Delivery> findByOrderId(UUID orderId);
    List<Delivery> findByDriverIdAndStatus(UUID driverId, DeliveryStatus status);
    List<Delivery> findByDriverIdAndStatusIn(UUID driverId, List<DeliveryStatus> statuses);

    @Query("SELECT COUNT(d) FROM Delivery d WHERE d.driverId = :driverId " +
           "AND d.status = 'DELIVERED' AND d.deliveredTime BETWEEN :from AND :to")
    long countDeliveredByDriverInRange(UUID driverId, LocalDateTime from, LocalDateTime to);
}

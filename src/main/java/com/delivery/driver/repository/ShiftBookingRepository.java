package com.delivery.driver.repository;

import com.delivery.driver.domain.ShiftBooking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ShiftBookingRepository extends JpaRepository<ShiftBooking, UUID> {
    boolean existsByDriverIdAndShiftId(UUID driverId, UUID shiftId);
    long countByShiftId(UUID shiftId);
    List<ShiftBooking> findAllByDriverId(UUID driverId);
}

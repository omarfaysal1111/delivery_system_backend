package com.delivery.driver.service;

import com.delivery.delivery.domain.DeliveryStatus;
import com.delivery.delivery.repository.DeliveryRepository;
import com.delivery.driver.dto.DriverStatsDto;
import com.delivery.driver.repository.DriverEarningsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DriverStatsService {

    private final DeliveryRepository deliveryRepository;
    private final DriverEarningsRepository driverEarningsRepository;

    public DriverStatsDto getTodayStats(UUID driverId) {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime now = LocalDateTime.now();

        long totalTrips = deliveryRepository.countDeliveredByDriverInRange(driverId, start, now);
        BigDecimal totalEarnings = driverEarningsRepository.sumByDriverAndDateRange(driverId, start, now);

        long totalDelivered = deliveryRepository.findByDriverIdAndStatus(driverId, DeliveryStatus.DELIVERED).size();
        double completionRate = totalDelivered > 0 ? 100.0 : 0.0;

        return DriverStatsDto.builder()
                .totalTripsToday(totalTrips)
                .totalEarningsToday(totalEarnings != null ? totalEarnings : BigDecimal.ZERO)
                .hoursOnline(0.0)
                .acceptanceRate(100.0)
                .completionRate(completionRate)
                .build();
    }
}

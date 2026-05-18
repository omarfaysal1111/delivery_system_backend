package com.delivery.driver.service;

import com.delivery.delivery.domain.DeliveryStatus;
import com.delivery.delivery.repository.DeliveryRepository;
import com.delivery.driver.dto.EarningsSummaryDto;
import com.delivery.driver.dto.EarningsTripDto;
import com.delivery.driver.dto.IncentiveDto;
import com.delivery.driver.dto.PerformanceDto;
import com.delivery.driver.repository.DriverEarningsRepository;
import com.delivery.driver.repository.IncentiveRepository;
import com.delivery.review.domain.TargetType;
import com.delivery.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class EarningsService {

    private final DriverEarningsRepository earningsRepository;
    private final IncentiveRepository incentiveRepository;
    private final DeliveryRepository deliveryRepository;
    private final ReviewRepository reviewRepository;

    public EarningsSummaryDto getSummary(UUID driverId, String period) {
        LocalDateTime[] range = resolveRange(period);
        LocalDateTime from = range[0], to = range[1];

        BigDecimal tripsAmount = earningsRepository.sumByDriverTypeAndDateRange(driverId, "trip", from, to);
        BigDecimal tipsAmount = earningsRepository.sumByDriverTypeAndDateRange(driverId, "tip", from, to);
        BigDecimal bonusAmount = earningsRepository.sumByDriverTypeAndDateRange(driverId, "bonus", from, to);
        BigDecimal totalEarnings = sum(tripsAmount, tipsAmount, bonusAmount);
        BigDecimal totalWithdrawals = earningsRepository.totalWithdrawalsByDriver(driverId);
        BigDecimal withdrawable = totalEarnings.subtract(totalWithdrawals != null ? totalWithdrawals : BigDecimal.ZERO);

        return EarningsSummaryDto.builder()
                .totalEarnings(totalEarnings)
                .periodLabel(period)
                .tripsAmount(orZero(tripsAmount))
                .tipsAmount(orZero(tipsAmount))
                .bonusAmount(orZero(bonusAmount))
                .withdrawableBalance(withdrawable.max(BigDecimal.ZERO))
                .pendingBalance(BigDecimal.ZERO)
                .build();
    }

    public List<EarningsTripDto> getHistory(UUID driverId, LocalDateTime from, LocalDateTime to) {
        return earningsRepository.findByDriverIdOrderByCreatedAtDesc(driverId, PageRequest.of(0, 50))
                .stream()
                .filter(e -> (from == null || !e.getCreatedAt().isBefore(from))
                        && (to == null || !e.getCreatedAt().isAfter(to)))
                .map(EarningsTripDto::from)
                .toList();
    }

    public List<IncentiveDto> getIncentives(UUID driverId) {
        long completedTrips = deliveryRepository.findByDriverIdAndStatus(driverId, DeliveryStatus.DELIVERED).size();
        return incentiveRepository.findAllByIsActiveTrue().stream()
                .map(inc -> IncentiveDto.from(inc, completedTrips))
                .toList();
    }

    public PerformanceDto getPerformance(UUID driverId) {
        Double avgRating = reviewRepository.avgRatingByTarget(TargetType.DRIVER, driverId);
        long delivered = deliveryRepository.findByDriverIdAndStatus(driverId, DeliveryStatus.DELIVERED).size();

        List<Long> weeklyTrend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDateTime dayStart = LocalDate.now().minusDays(i).atStartOfDay();
            LocalDateTime dayEnd = dayStart.plusDays(1);
            weeklyTrend.add(deliveryRepository.countDeliveredByDriverInRange(driverId, dayStart, dayEnd));
        }

        long maxWeekly = weeklyTrend.stream().mapToLong(Long::longValue).max().orElse(1L);
        double completionRate = delivered > 0 ? 100.0 : 0.0;

        return PerformanceDto.builder()
                .acceptanceRate(100.0)
                .completionRate(completionRate)
                .averageRating(avgRating != null ? avgRating : 0.0)
                .onTimeRate(95.0)
                .weeklyTrend(weeklyTrend)
                .build();
    }

    public void requestWithdrawal(UUID driverId) {
        // stub — log withdrawal request
    }

    private LocalDateTime[] resolveRange(String period) {
        LocalDate today = LocalDate.now();
        return switch (period != null ? period.toLowerCase() : "today") {
            case "week" -> new LocalDateTime[]{
                    today.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY)).atStartOfDay(),
                    LocalDateTime.now()
            };
            case "month" -> new LocalDateTime[]{
                    today.withDayOfMonth(1).atStartOfDay(),
                    LocalDateTime.now()
            };
            default -> new LocalDateTime[]{today.atStartOfDay(), LocalDateTime.now()};
        };
    }

    private BigDecimal orZero(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    private BigDecimal sum(BigDecimal... values) {
        BigDecimal total = BigDecimal.ZERO;
        for (BigDecimal v : values) if (v != null) total = total.add(v);
        return total;
    }
}

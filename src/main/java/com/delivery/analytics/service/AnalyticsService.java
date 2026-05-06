package com.delivery.analytics.service;

import com.delivery.analytics.dto.AnalyticsSummaryDto;
import com.delivery.order.repository.OrderRepository;
import com.delivery.payment.repository.TransactionRepository;
import com.delivery.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final TransactionRepository transactionRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public AnalyticsSummaryDto getSummary(LocalDate from, LocalDate to) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.plusDays(1).atStartOfDay();

        BigDecimal revenue = transactionRepository.sumRevenueByDateRange(start, end);
        long orderCount = orderRepository.countByCreatedAtBetween(start, end);
        long newCustomers = userRepository.countByCreatedAtBetween(start, end);
        double avgOrder = (orderCount > 0)
                ? revenue.divide(BigDecimal.valueOf(orderCount), 2, java.math.RoundingMode.HALF_UP).doubleValue()
                : 0.0;

        return AnalyticsSummaryDto.builder()
                .from(from).to(to)
                .totalRevenue(revenue)
                .totalOrders(orderCount)
                .newCustomers(newCustomers)
                .avgOrderValue(avgOrder)
                .build();
    }
}

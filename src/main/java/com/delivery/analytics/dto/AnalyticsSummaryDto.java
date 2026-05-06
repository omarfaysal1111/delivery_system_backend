package com.delivery.analytics.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class AnalyticsSummaryDto {
    private LocalDate from;
    private LocalDate to;
    private BigDecimal totalRevenue;
    private long totalOrders;
    private long newCustomers;
    private double avgOrderValue;
}

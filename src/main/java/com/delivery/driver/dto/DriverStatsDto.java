package com.delivery.driver.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class DriverStatsDto {
    private long totalTripsToday;
    private BigDecimal totalEarningsToday;
    private double hoursOnline;
    private double acceptanceRate;
    private double completionRate;
}

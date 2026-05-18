package com.delivery.driver.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PerformanceDto {
    private double acceptanceRate;
    private double completionRate;
    private double averageRating;
    private double onTimeRate;
    private List<Long> weeklyTrend;
}

package com.delivery.driver.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class EarningsSummaryDto {
    private BigDecimal totalEarnings;
    private String periodLabel;
    private BigDecimal tripsAmount;
    private BigDecimal tipsAmount;
    private BigDecimal bonusAmount;
    private BigDecimal withdrawableBalance;
    private BigDecimal pendingBalance;
}

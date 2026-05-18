package com.delivery.driver.dto;

import com.delivery.driver.domain.DriverEarnings;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class EarningsTripDto {
    private UUID id;
    private LocalDateTime date;
    private UUID orderId;
    private BigDecimal amount;
    private String type;
    private String description;

    public static EarningsTripDto from(DriverEarnings e) {
        return EarningsTripDto.builder()
                .id(e.getId()).date(e.getCreatedAt())
                .orderId(e.getOrderId()).amount(e.getAmount())
                .type(e.getType()).description(e.getDescription())
                .build();
    }
}

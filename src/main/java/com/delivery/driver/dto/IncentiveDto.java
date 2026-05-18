package com.delivery.driver.dto;

import com.delivery.driver.domain.Incentive;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class IncentiveDto {
    private UUID id;
    private String title;
    private String description;
    private int targetTrips;
    private BigDecimal bonusAmount;
    private LocalDateTime deadline;
    private boolean isActive;
    private long completedTrips;
    private boolean isCompleted;

    public static IncentiveDto from(Incentive inc, long completedTrips) {
        return IncentiveDto.builder()
                .id(inc.getId()).title(inc.getTitle())
                .description(inc.getDescription()).targetTrips(inc.getTargetTrips())
                .bonusAmount(inc.getBonusAmount()).deadline(inc.getDeadline())
                .isActive(inc.isActive()).completedTrips(completedTrips)
                .isCompleted(completedTrips >= inc.getTargetTrips())
                .build();
    }
}

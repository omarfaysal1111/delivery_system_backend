package com.delivery.driver.dto;

import com.delivery.driver.domain.Shift;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Builder
public class ShiftDto {
    private UUID id;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private int maxDrivers;
    private long bookedCount;
    private boolean isBookedByMe;

    public static ShiftDto from(Shift s, long bookedCount, boolean bookedByMe) {
        return ShiftDto.builder()
                .id(s.getId()).date(s.getDate())
                .startTime(s.getStartTime()).endTime(s.getEndTime())
                .maxDrivers(s.getMaxDrivers())
                .bookedCount(bookedCount).isBookedByMe(bookedByMe)
                .build();
    }
}

package com.delivery.driver.dto;

import com.delivery.driver.domain.ShiftBooking;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class ShiftBookingDto {
    private UUID id;
    private UUID driverId;
    private UUID shiftId;
    private LocalDateTime bookedAt;

    public static ShiftBookingDto from(ShiftBooking sb) {
        return ShiftBookingDto.builder()
                .id(sb.getId()).driverId(sb.getDriverId())
                .shiftId(sb.getShiftId()).bookedAt(sb.getBookedAt())
                .build();
    }
}

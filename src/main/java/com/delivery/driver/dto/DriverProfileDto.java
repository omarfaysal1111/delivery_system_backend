package com.delivery.driver.dto;

import com.delivery.user.domain.DriverProfile;
import com.delivery.user.domain.DriverStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class DriverProfileDto {
    private UUID id;
    private UUID userId;
    private String name;
    private String phone;
    private String vehicleType;
    private String licensePlate;
    private String nationalId;
    private String profilePhotoUrl;
    private Double avgRating;
    private DriverStatus status;
    private boolean isOnline;

    public static DriverProfileDto from(DriverProfile dp, String name, String phone) {
        return DriverProfileDto.builder()
                .id(dp.getId()).userId(dp.getUserId())
                .name(name).phone(phone)
                .vehicleType(dp.getVehicleType())
                .licensePlate(dp.getLicensePlate())
                .nationalId(dp.getNationalId())
                .profilePhotoUrl(dp.getProfilePhotoUrl())
                .avgRating(dp.getAvgRating())
                .status(dp.getStatus())
                .isOnline(dp.isOnline())
                .build();
    }
}

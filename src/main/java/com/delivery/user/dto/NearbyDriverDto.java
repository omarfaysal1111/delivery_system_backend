package com.delivery.user.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class NearbyDriverDto {
    private UUID driverId;
    private UUID userId;
    private String vehicleType;
    private Double avgRating;
    private Double lat;
    private Double lng;
    private Double distanceKm;
}

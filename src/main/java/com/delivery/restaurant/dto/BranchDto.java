package com.delivery.restaurant.dto;

import com.delivery.restaurant.domain.Branch;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;
import java.util.UUID;

@Getter
@Builder
public class BranchDto {
    private UUID id;
    private UUID restaurantId;
    private String address;
    private Double lat;
    private Double lng;
    private Map<String, Object> operatingHours;
    private boolean isActive;

    public static BranchDto from(Branch b) {
        return BranchDto.builder()
                .id(b.getId()).restaurantId(b.getRestaurantId())
                .address(b.getAddress()).lat(b.getLat()).lng(b.getLng())
                .operatingHours(b.getOperatingHours()).isActive(b.isActive())
                .build();
    }
}

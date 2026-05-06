package com.delivery.restaurant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class CreateBranchRequest {
    @NotNull  private UUID restaurantId;
    @NotBlank private String address;
    private Double lat;
    private Double lng;
    private Map<String, Object> operatingHours;
}

package com.delivery.delivery.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LocationUpdateRequest {
    @NotNull private Double lat;
    @NotNull private Double lng;
}

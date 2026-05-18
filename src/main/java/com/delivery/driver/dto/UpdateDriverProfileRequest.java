package com.delivery.driver.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateDriverProfileRequest {
    private String vehicleType;
    private String vehicleNumber;
    private String profilePhotoUrl;
}

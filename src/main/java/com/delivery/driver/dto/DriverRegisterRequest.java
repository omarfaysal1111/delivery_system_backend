package com.delivery.driver.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DriverRegisterRequest {
    private String vehicleType;
    private String vehicleNumber;
    private String nationalId;
}

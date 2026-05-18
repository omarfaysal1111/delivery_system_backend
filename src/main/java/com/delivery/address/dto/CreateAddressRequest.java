package com.delivery.address.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateAddressRequest {
    private String label;
    private String fullAddress;
    private Double lat;
    private Double lng;
    private String city;
    private String neighborhood;
    private boolean isDefault;
}

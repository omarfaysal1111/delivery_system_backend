package com.delivery.address.dto;

import com.delivery.address.domain.UserAddress;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class AddressDto {
    private UUID id;
    private String label;
    private String fullAddress;
    private Double lat;
    private Double lng;
    private String city;
    private String neighborhood;
    private boolean isDefault;

    public static AddressDto from(UserAddress a) {
        return AddressDto.builder()
                .id(a.getId()).label(a.getLabel())
                .fullAddress(a.getFullAddress()).lat(a.getLat()).lng(a.getLng())
                .city(a.getCity()).neighborhood(a.getNeighborhood())
                .isDefault(a.isDefault())
                .build();
    }
}

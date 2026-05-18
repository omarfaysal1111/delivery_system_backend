package com.delivery.driver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZoneDto {
    private UUID id;
    private String label;
    private double multiplier;
    private String color;
    private List<Map<String, Double>> polygon;
    private double centerLat;
    private double centerLng;
}

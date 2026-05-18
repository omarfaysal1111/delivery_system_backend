package com.delivery.driver.service;

import com.delivery.driver.dto.ZoneDto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ZoneService {

    public List<ZoneDto> getDemoZones() {
        return List.of(
                ZoneDto.builder()
                        .id(UUID.randomUUID()).label("Zone A").multiplier(1.5).color("#FF5733")
                        .centerLat(30.0).centerLng(31.0)
                        .polygon(List.of(
                                Map.of("lat", 30.01, "lng", 30.99),
                                Map.of("lat", 30.01, "lng", 31.01),
                                Map.of("lat", 29.99, "lng", 31.01),
                                Map.of("lat", 29.99, "lng", 30.99)
                        )).build(),
                ZoneDto.builder()
                        .id(UUID.randomUUID()).label("Zone B").multiplier(2.0).color("#33C3FF")
                        .centerLat(30.05).centerLng(31.05)
                        .polygon(List.of(
                                Map.of("lat", 30.06, "lng", 31.04),
                                Map.of("lat", 30.06, "lng", 31.06),
                                Map.of("lat", 30.04, "lng", 31.06),
                                Map.of("lat", 30.04, "lng", 31.04)
                        )).build(),
                ZoneDto.builder()
                        .id(UUID.randomUUID()).label("Zone C").multiplier(1.2).color("#33FF57")
                        .centerLat(29.95).centerLng(30.95)
                        .polygon(List.of(
                                Map.of("lat", 29.96, "lng", 30.94),
                                Map.of("lat", 29.96, "lng", 30.96),
                                Map.of("lat", 29.94, "lng", 30.96),
                                Map.of("lat", 29.94, "lng", 30.94)
                        )).build()
        );
    }
}

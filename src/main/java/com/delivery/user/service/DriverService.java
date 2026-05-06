package com.delivery.user.service;

import com.delivery.user.domain.DriverProfile;
import com.delivery.user.dto.NearbyDriverDto;
import com.delivery.user.repository.DriverProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DriverService {

    private final DriverProfileRepository driverProfileRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${app.driver.default-search-radius-km:5.0}")
    private double defaultRadiusKm;

    private static final String GEO_KEY = "drivers:geo";

    public List<NearbyDriverDto> findNearby(double lat, double lng, Double radiusKm) {
        double radius = radiusKm != null ? radiusKm : defaultRadiusKm;

        Circle area = new Circle(new Point(lng, lat), new Distance(radius, Metrics.KILOMETERS));
        GeoResults<RedisGeoCommands.GeoLocation<String>> results =
                redisTemplate.opsForGeo().radius(GEO_KEY, area,
                        RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                                .includeCoordinates()
                                .includeDistance()
                                .sortAscending());

        if (results == null) return List.of();

        List<NearbyDriverDto> dtos = new ArrayList<>();
        for (GeoResult<RedisGeoCommands.GeoLocation<String>> result : results) {
            UUID userId = UUID.fromString(result.getContent().getName());
            driverProfileRepository.findByUserId(userId).ifPresent(dp -> {
                Point pos = result.getContent().getPoint();
                dtos.add(NearbyDriverDto.builder()
                        .driverId(dp.getId())
                        .userId(userId)
                        .vehicleType(dp.getVehicleType())
                        .avgRating(dp.getAvgRating())
                        .lat(pos != null ? pos.getY() : dp.getLastLat())
                        .lng(pos != null ? pos.getX() : dp.getLastLng())
                        .distanceKm(result.getDistance().getValue())
                        .build());
            });
        }
        return dtos;
    }

    public DriverProfile registerDriver(UUID userId, String vehicleType, String licensePlate) {
        DriverProfile profile = DriverProfile.builder()
                .userId(userId)
                .vehicleType(vehicleType)
                .licensePlate(licensePlate)
                .build();
        return driverProfileRepository.save(profile);
    }
}

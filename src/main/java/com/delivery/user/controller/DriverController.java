package com.delivery.user.controller;

import com.delivery.user.dto.NearbyDriverDto;
import com.delivery.user.service.DriverService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/drivers")
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;

    /**
     * GET /api/v1/drivers/nearby?lat=...&lng=...&radiusKm=...
     * Returns online drivers within the given radius (default 5 km).
     * Used internally by the delivery assignment algorithm.
     */
    @GetMapping("/nearby")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESTAURANT_OWNER')")
    public ResponseEntity<List<NearbyDriverDto>> nearby(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(required = false) Double radiusKm) {
        return ResponseEntity.ok(driverService.findNearby(lat, lng, radiusKm));
    }
}

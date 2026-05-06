package com.delivery.delivery.controller;

import com.delivery.common.security.UserPrincipal;
import com.delivery.delivery.dto.LocationUpdateRequest;
import com.delivery.delivery.service.DeliveryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    @PutMapping("/{id}/locate")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<Map<String, String>> updateLocation(
            @PathVariable UUID id,
            @Valid @RequestBody LocationUpdateRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        deliveryService.updateLocation(id, principal.getId(), req);
        return ResponseEntity.ok(Map.of("status", "location_updated"));
    }

    @PostMapping("/{id}/pickup")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<Void> markPickedUp(@PathVariable UUID id) {
        deliveryService.markPickedUp(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/delivered")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<Void> markDelivered(@PathVariable UUID id) {
        deliveryService.markDelivered(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/online")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<Void> setOnlineStatus(
            @RequestParam boolean online,
            @AuthenticationPrincipal UserPrincipal principal) {
        deliveryService.setDriverOnline(principal.getId(), online);
        return ResponseEntity.noContent().build();
    }
}

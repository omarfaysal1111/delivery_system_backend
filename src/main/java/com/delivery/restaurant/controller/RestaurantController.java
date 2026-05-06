package com.delivery.restaurant.controller;

import com.delivery.common.dto.PageResponse;
import com.delivery.common.security.UserPrincipal;
import com.delivery.restaurant.dto.*;
import com.delivery.restaurant.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;

    @GetMapping
    public ResponseEntity<PageResponse<RestaurantDto>> list(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {
        if (search != null && !search.isBlank()) {
            return ResponseEntity.ok(restaurantService.search(search, pageable));
        }
        return ResponseEntity.ok(restaurantService.listActive(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestaurantDto> get(@PathVariable UUID id) {
        return ResponseEntity.ok(restaurantService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER', 'ADMIN')")
    public ResponseEntity<RestaurantDto> create(
            @Valid @RequestBody CreateRestaurantRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(restaurantService.create(principal.getId(), req));
    }

    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER', 'ADMIN')")
    public ResponseEntity<RestaurantDto> toggle(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(restaurantService.toggleActive(id, principal.getId()));
    }

    // Branch endpoints

    @GetMapping("/{restaurantId}/branches")
    public ResponseEntity<List<BranchDto>> getBranches(@PathVariable UUID restaurantId) {
        return ResponseEntity.ok(restaurantService.getBranches(restaurantId));
    }

    @PostMapping("/branches")
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER', 'ADMIN')")
    public ResponseEntity<BranchDto> createBranch(@Valid @RequestBody CreateBranchRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(restaurantService.createBranch(req));
    }

    @PutMapping("/branches/{branchId}/hours")
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER', 'ADMIN')")
    public ResponseEntity<BranchDto> updateHours(
            @PathVariable UUID branchId,
            @RequestBody Map<String, Object> hours) {
        return ResponseEntity.ok(restaurantService.updateOperatingHours(branchId, hours));
    }
}

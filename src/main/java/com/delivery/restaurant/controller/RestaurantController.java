package com.delivery.restaurant.controller;

import com.delivery.common.dto.PageResponse;
import com.delivery.common.security.UserPrincipal;
import com.delivery.favorite.service.FavoriteService;
import com.delivery.menu.service.MenuService;
import com.delivery.offer.dto.CreateOfferRequest;
import com.delivery.offer.dto.OfferDto;
import com.delivery.offer.service.OfferService;
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
    private final MenuService menuService;
    private final OfferService offerService;
    private final FavoriteService favoriteService;

    @GetMapping
    public ResponseEntity<PageResponse<RestaurantDto>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String cuisineType,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Integer maxDeliveryTime,
            @PageableDefault(size = 20) Pageable pageable) {
        if (search != null && !search.isBlank()) {
            return ResponseEntity.ok(restaurantService.search(search, pageable));
        }
        if (cuisineType != null || minRating != null || maxDeliveryTime != null) {
            return ResponseEntity.ok(restaurantService.filterActive(cuisineType, minRating, maxDeliveryTime, pageable));
        }
        return ResponseEntity.ok(restaurantService.listActive(pageable));
    }

    @GetMapping("/most-ordered")
    public ResponseEntity<PageResponse<RestaurantDto>> mostOrdered(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(restaurantService.getMostOrdered(pageable));
    }

    @GetMapping("/top-rated")
    public ResponseEntity<PageResponse<RestaurantDto>> topRated(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(restaurantService.getTopRated(pageable));
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

    // Menu by restaurant

    @GetMapping("/{restaurantId}/menu")
    public ResponseEntity<List<com.delivery.menu.dto.MenuCategoryDto>> getMenu(
            @PathVariable UUID restaurantId) {
        return ResponseEntity.ok(menuService.getMenuByRestaurant(restaurantId));
    }

    // Offer endpoints

    @GetMapping("/{id}/offers")
    public ResponseEntity<List<OfferDto>> getOffers(@PathVariable UUID id) {
        return ResponseEntity.ok(offerService.getOffers(id));
    }

    @PostMapping("/{id}/offers")
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER', 'ADMIN')")
    public ResponseEntity<OfferDto> createOffer(@PathVariable UUID id,
                                                  @RequestBody CreateOfferRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(offerService.createOffer(id, req));
    }

    // Favorite endpoints

    @PostMapping("/{id}/favorite")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Map<String, Boolean>> addFavorite(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(favoriteService.addFavorite(principal.getId(), id));
    }

    @DeleteMapping("/{id}/favorite")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Void> removeFavorite(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal) {
        favoriteService.removeFavorite(principal.getId(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/favorites")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<RestaurantDto>> getFavorites(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(favoriteService.getFavorites(principal.getId()));
    }
}

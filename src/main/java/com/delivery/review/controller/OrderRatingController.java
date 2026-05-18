package com.delivery.review.controller;

import com.delivery.common.security.UserPrincipal;
import com.delivery.review.dto.OrderRatingRequest;
import com.delivery.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderRatingController {

    private final ReviewService reviewService;

    @PostMapping("/{orderId}/rating")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Void> submitRating(
            @PathVariable UUID orderId,
            @Valid @RequestBody OrderRatingRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        reviewService.submitOrderRating(principal.getId(), orderId, req);
        return ResponseEntity.ok().build();
    }
}

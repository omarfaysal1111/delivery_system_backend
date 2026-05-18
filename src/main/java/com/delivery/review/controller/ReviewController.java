package com.delivery.review.controller;

import com.delivery.common.dto.PageResponse;
import com.delivery.common.security.UserPrincipal;
import com.delivery.review.domain.TargetType;
import com.delivery.review.dto.CreateReviewRequest;
import com.delivery.review.dto.OrderRatingRequest;
import com.delivery.review.dto.ReviewDto;
import com.delivery.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ReviewDto> create(
            @Valid @RequestBody CreateReviewRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewService.create(principal.getId(), req));
    }

    @GetMapping
    public ResponseEntity<PageResponse<ReviewDto>> list(
            @RequestParam TargetType targetType,
            @RequestParam UUID targetId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(reviewService.getReviews(targetType, targetId, pageable));
    }

    @GetMapping("/rating")
    public ResponseEntity<Map<String, Object>> getRating(
            @RequestParam TargetType targetType,
            @RequestParam UUID targetId) {
        Double avg = reviewService.getAverageRating(targetType, targetId);
        return ResponseEntity.ok(Map.of("targetType", targetType, "targetId", targetId, "avgRating", avg));
    }
}

// Order rating endpoint lives under /api/v1/orders/{orderId}/rating — see OrderRatingController

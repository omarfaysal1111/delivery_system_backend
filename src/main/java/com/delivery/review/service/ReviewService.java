package com.delivery.review.service;

import com.delivery.common.dto.PageResponse;
import com.delivery.common.exception.AuthException;
import com.delivery.common.exception.ConflictException;
import com.delivery.common.exception.ResourceNotFoundException;
import com.delivery.delivery.repository.DeliveryRepository;
import com.delivery.order.domain.Order;
import com.delivery.order.domain.OrderStatus;
import com.delivery.order.service.OrderService;
import com.delivery.restaurant.repository.BranchRepository;
import com.delivery.review.domain.Review;
import com.delivery.review.domain.TargetType;
import com.delivery.review.dto.CreateReviewRequest;
import com.delivery.review.dto.OrderRatingRequest;
import com.delivery.review.dto.ReviewDto;
import com.delivery.review.repository.ReviewRepository;
import com.delivery.user.domain.DriverProfile;
import com.delivery.user.repository.DriverProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderService orderService;
    private final DeliveryRepository deliveryRepository;
    private final BranchRepository branchRepository;
    private final DriverProfileRepository driverProfileRepository;

    @Transactional
    public ReviewDto create(UUID reviewerId, CreateReviewRequest req) {
        if (reviewRepository.existsByReviewerIdAndTargetTypeAndTargetId(
                reviewerId, req.getTargetType(), req.getTargetId())) {
            throw new AuthException("You have already reviewed this " + req.getTargetType().name().toLowerCase());
        }
        Review review = Review.builder()
                .reviewerId(reviewerId)
                .targetType(req.getTargetType())
                .targetId(req.getTargetId())
                .rating(req.getRating())
                .comment(req.getComment())
                .build();
        return ReviewDto.from(reviewRepository.save(review));
    }

    public PageResponse<ReviewDto> getReviews(TargetType targetType, UUID targetId, Pageable pageable) {
        return PageResponse.of(
                reviewRepository.findAllByTargetTypeAndTargetId(targetType, targetId, pageable)
                        .map(ReviewDto::from));
    }

    public Double getAverageRating(TargetType targetType, UUID targetId) {
        Double avg = reviewRepository.avgRatingByTarget(targetType, targetId);
        return avg != null ? avg : 0.0;
    }

    @Transactional
    public void submitOrderRating(UUID customerId, UUID orderId, OrderRatingRequest req) {
        Order order = orderService.findOrder(orderId);
        if (!order.getCustomerId().equals(customerId)) {
            throw new AuthException("Not authorized to rate this order");
        }
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new ConflictException("Order must be delivered before rating");
        }

        var delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found for order"));

        var branch = branchRepository.findById(order.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));

        if (!reviewRepository.existsByReviewerIdAndTargetTypeAndTargetId(
                customerId, TargetType.RESTAURANT, branch.getRestaurantId())) {
            reviewRepository.save(Review.builder()
                    .reviewerId(customerId)
                    .targetType(TargetType.RESTAURANT)
                    .targetId(branch.getRestaurantId())
                    .rating(req.getRestaurantRating())
                    .comment(req.getComment())
                    .build());
        }

        if (req.getDriverRating() != null && delivery.getDriverId() != null) {
            if (!reviewRepository.existsByReviewerIdAndTargetTypeAndTargetId(
                    customerId, TargetType.DRIVER, delivery.getDriverId())) {
                reviewRepository.save(Review.builder()
                        .reviewerId(customerId)
                        .targetType(TargetType.DRIVER)
                        .targetId(delivery.getDriverId())
                        .rating(req.getDriverRating())
                        .comment(req.getComment())
                        .build());

                driverProfileRepository.findByUserId(delivery.getDriverId()).ifPresent(dp -> {
                    Double avgRating = reviewRepository.avgRatingByTarget(TargetType.DRIVER, delivery.getDriverId());
                    dp.setAvgRating(avgRating != null ? avgRating : dp.getAvgRating());
                    driverProfileRepository.save(dp);
                });
            }
        }
    }
}

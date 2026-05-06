package com.delivery.review.service;

import com.delivery.common.dto.PageResponse;
import com.delivery.common.exception.AuthException;
import com.delivery.review.domain.Review;
import com.delivery.review.domain.TargetType;
import com.delivery.review.dto.CreateReviewRequest;
import com.delivery.review.dto.ReviewDto;
import com.delivery.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;

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
}

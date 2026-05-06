package com.delivery.review.dto;

import com.delivery.review.domain.Review;
import com.delivery.review.domain.TargetType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class ReviewDto {
    private UUID id;
    private UUID reviewerId;
    private TargetType targetType;
    private UUID targetId;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;

    public static ReviewDto from(Review r) {
        return ReviewDto.builder()
                .id(r.getId()).reviewerId(r.getReviewerId())
                .targetType(r.getTargetType()).targetId(r.getTargetId())
                .rating(r.getRating()).comment(r.getComment())
                .createdAt(r.getCreatedAt()).build();
    }
}

package com.delivery.review.repository;

import com.delivery.review.domain.Review;
import com.delivery.review.domain.TargetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {
    Page<Review> findAllByTargetTypeAndTargetId(TargetType targetType, UUID targetId, Pageable pageable);
    boolean existsByReviewerIdAndTargetTypeAndTargetId(UUID reviewerId, TargetType targetType, UUID targetId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.targetType = :targetType AND r.targetId = :targetId")
    Double avgRatingByTarget(TargetType targetType, UUID targetId);
}

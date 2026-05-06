package com.delivery.restaurant.repository;

import com.delivery.restaurant.domain.Branch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BranchRepository extends JpaRepository<Branch, UUID> {
    List<Branch> findAllByRestaurantId(UUID restaurantId);
    List<Branch> findAllByRestaurantIdAndIsActiveTrue(UUID restaurantId);
}

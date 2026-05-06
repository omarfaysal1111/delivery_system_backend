package com.delivery.restaurant.repository;

import com.delivery.restaurant.domain.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RestaurantRepository extends JpaRepository<Restaurant, UUID> {
    Optional<Restaurant> findBySlug(String slug);
    boolean existsBySlug(String slug);
    Page<Restaurant> findAllByIsActiveTrue(Pageable pageable);
    Page<Restaurant> findAllByIsActiveTrueAndCuisineTypeIgnoreCase(String cuisineType, Pageable pageable);
    Page<Restaurant> findAllByIsActiveTrueAndNameContainingIgnoreCase(String name, Pageable pageable);
    List<Restaurant> findAllByOwnerId(UUID ownerId);
}

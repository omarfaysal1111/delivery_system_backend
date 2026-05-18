package com.delivery.restaurant.repository;

import com.delivery.restaurant.domain.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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

    @Query("SELECT r FROM Restaurant r WHERE r.isActive = true " +
           "ORDER BY (SELECT COUNT(o) FROM Order o WHERE o.branchId IN " +
           "(SELECT b.id FROM Branch b WHERE b.restaurantId = r.id)) DESC")
    Page<Restaurant> findMostOrdered(Pageable pageable);

    @Query("SELECT r FROM Restaurant r WHERE r.isActive = true " +
           "AND (:cuisineType IS NULL OR LOWER(r.cuisineType) LIKE LOWER(CONCAT('%',:cuisineType,'%'))) " +
           "AND (:maxDeliveryTime IS NULL OR r.deliveryTimeMax <= :maxDeliveryTime) " +
           "ORDER BY r.name")
    Page<Restaurant> filterActive(String cuisineType, Integer maxDeliveryTime, Pageable pageable);

    @Query("SELECT r FROM Restaurant r WHERE r.isActive = true " +
           "ORDER BY (SELECT COALESCE(AVG(rv.rating), 0) FROM Review rv " +
           "WHERE rv.targetType = 'RESTAURANT' AND rv.targetId = r.id) DESC")
    Page<Restaurant> findTopRated(Pageable pageable);
}

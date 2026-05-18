package com.delivery.favorite.repository;

import com.delivery.favorite.domain.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FavoriteRepository extends JpaRepository<Favorite, UUID> {
    boolean existsByUserIdAndRestaurantId(UUID userId, UUID restaurantId);
    Optional<Favorite> findByUserIdAndRestaurantId(UUID userId, UUID restaurantId);
    List<Favorite> findAllByUserId(UUID userId);
}

package com.delivery.favorite.service;

import com.delivery.favorite.domain.Favorite;
import com.delivery.favorite.repository.FavoriteRepository;
import com.delivery.restaurant.domain.Restaurant;
import com.delivery.restaurant.dto.RestaurantDto;
import com.delivery.restaurant.repository.RestaurantRepository;
import com.delivery.review.domain.TargetType;
import com.delivery.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final RestaurantRepository restaurantRepository;
    private final ReviewRepository reviewRepository;

    @Transactional
    public Map<String, Boolean> addFavorite(UUID userId, UUID restaurantId) {
        if (!favoriteRepository.existsByUserIdAndRestaurantId(userId, restaurantId)) {
            favoriteRepository.save(Favorite.builder()
                    .userId(userId).restaurantId(restaurantId).build());
        }
        return Map.of("isFavorite", true);
    }

    @Transactional
    public void removeFavorite(UUID userId, UUID restaurantId) {
        favoriteRepository.findByUserIdAndRestaurantId(userId, restaurantId)
                .ifPresent(favoriteRepository::delete);
    }

    public List<RestaurantDto> getFavorites(UUID userId) {
        List<UUID> ids = favoriteRepository.findAllByUserId(userId)
                .stream().map(Favorite::getRestaurantId).toList();
        return restaurantRepository.findAllById(ids)
                .stream().map(r -> enrich(r)).toList();
    }

    private RestaurantDto enrich(Restaurant r) {
        Double rating = reviewRepository.avgRatingByTarget(TargetType.RESTAURANT, r.getId());
        Long count = reviewRepository.countByTarget(TargetType.RESTAURANT, r.getId());
        return RestaurantDto.from(r, rating != null ? rating : 0.0, count != null ? count : 0L, false);
    }
}

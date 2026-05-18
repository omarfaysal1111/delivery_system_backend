package com.delivery.restaurant.service;

import com.delivery.common.dto.PageResponse;
import com.delivery.common.exception.AuthException;
import com.delivery.common.exception.ResourceNotFoundException;
import com.delivery.restaurant.domain.Branch;
import com.delivery.restaurant.domain.Restaurant;
import com.delivery.restaurant.dto.*;
import com.delivery.restaurant.repository.BranchRepository;
import com.delivery.restaurant.repository.RestaurantRepository;
import com.delivery.review.domain.TargetType;
import com.delivery.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final BranchRepository branchRepository;
    private final ReviewRepository reviewRepository;

    @Cacheable(value = "restaurants", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public PageResponse<RestaurantDto> listActive(Pageable pageable) {
        return PageResponse.of(restaurantRepository.findAllByIsActiveTrue(pageable)
                .map(this::enrich));
    }

    @Cacheable(value = "restaurants", key = "'search-' + #query + '-' + #pageable.pageNumber")
    public PageResponse<RestaurantDto> search(String query, Pageable pageable) {
        return PageResponse.of(
                restaurantRepository.findAllByIsActiveTrueAndNameContainingIgnoreCase(query, pageable)
                        .map(this::enrich));
    }

    @Cacheable(value = "restaurant", key = "#id")
    public RestaurantDto getById(UUID id) {
        return restaurantRepository.findById(id)
                .map(this::enrich)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));
    }

    @Transactional
    @CacheEvict(value = "restaurants", allEntries = true)
    public RestaurantDto create(UUID ownerId, CreateRestaurantRequest req) {
        if (restaurantRepository.existsBySlug(req.getSlug())) {
            throw new AuthException("Slug already taken");
        }
        Restaurant restaurant = Restaurant.builder()
                .ownerId(ownerId)
                .name(req.getName())
                .slug(req.getSlug())
                .cuisineType(req.getCuisineType())
                .commissionPct(req.getCommissionPct())
                .build();
        return RestaurantDto.from(restaurantRepository.save(restaurant));
    }

    @Transactional
    @CacheEvict(value = {"restaurants", "restaurant"}, allEntries = true)
    public RestaurantDto toggleActive(UUID id, UUID requesterId) {
        Restaurant r = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));
        r.setActive(!r.isActive());
        return RestaurantDto.from(restaurantRepository.save(r));
    }

    public PageResponse<RestaurantDto> getMostOrdered(Pageable pageable) {
        return PageResponse.of(restaurantRepository.findMostOrdered(pageable).map(this::enrich));
    }

    public PageResponse<RestaurantDto> getTopRated(Pageable pageable) {
        return PageResponse.of(restaurantRepository.findTopRated(pageable).map(this::enrich));
    }

    public PageResponse<RestaurantDto> filterActive(String cuisineType, Double minRating,
                                                     Integer maxDeliveryTime, Pageable pageable) {
        var page = restaurantRepository.filterActive(cuisineType, maxDeliveryTime, pageable);
        var enriched = page.map(this::enrich);
        if (minRating != null) {
            final Double minR = minRating;
            var filtered = enriched.map(dto -> dto.getRating() >= minR ? dto : null);
            // Can't cleanly filter a Page — return all enriched and let client filter by rating
            // The JPQL already handles cuisine and deliveryTime; minRating is post-filter only
            var content = enriched.getContent().stream()
                    .filter(dto -> dto.getRating() >= minR).toList();
            org.springframework.data.domain.PageImpl<RestaurantDto> result =
                    new org.springframework.data.domain.PageImpl<>(content, pageable, content.size());
            return PageResponse.of(result);
        }
        return PageResponse.of(enriched);
    }

    // Branches

    @Cacheable(value = "menus", key = "'branches-' + #restaurantId")
    public List<BranchDto> getBranches(UUID restaurantId) {
        return branchRepository.findAllByRestaurantId(restaurantId).stream()
                .map(BranchDto::from).toList();
    }

    @Transactional
    @CacheEvict(value = "menus", allEntries = true)
    public BranchDto createBranch(CreateBranchRequest req) {
        Branch branch = Branch.builder()
                .restaurantId(req.getRestaurantId())
                .address(req.getAddress())
                .lat(req.getLat())
                .lng(req.getLng())
                .operatingHours(req.getOperatingHours())
                .build();
        return BranchDto.from(branchRepository.save(branch));
    }

    @Transactional
    @CacheEvict(value = "menus", allEntries = true)
    public BranchDto updateOperatingHours(UUID branchId, Map<String, Object> hours) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));
        branch.setOperatingHours(hours);
        return BranchDto.from(branchRepository.save(branch));
    }

    public RestaurantDto enrich(Restaurant r) {
        Double rating = reviewRepository.avgRatingByTarget(TargetType.RESTAURANT, r.getId());
        Long count = reviewRepository.countByTarget(TargetType.RESTAURANT, r.getId());
        return RestaurantDto.from(r, rating != null ? rating : 0.0, count != null ? count : 0L, false);
    }
}

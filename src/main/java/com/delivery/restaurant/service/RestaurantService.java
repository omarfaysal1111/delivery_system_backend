package com.delivery.restaurant.service;

import com.delivery.common.dto.PageResponse;
import com.delivery.common.exception.AuthException;
import com.delivery.restaurant.domain.Branch;
import com.delivery.restaurant.domain.Restaurant;
import com.delivery.restaurant.dto.*;
import com.delivery.restaurant.repository.BranchRepository;
import com.delivery.restaurant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final BranchRepository branchRepository;

    @Cacheable(value = "restaurants", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public PageResponse<RestaurantDto> listActive(Pageable pageable) {
        return PageResponse.of(restaurantRepository.findAllByIsActiveTrue(pageable).map(RestaurantDto::from));
    }

    @Cacheable(value = "restaurants", key = "'search-' + #query + '-' + #pageable.pageNumber")
    public PageResponse<RestaurantDto> search(String query, Pageable pageable) {
        return PageResponse.of(
                restaurantRepository.findAllByIsActiveTrueAndNameContainingIgnoreCase(query, pageable)
                        .map(RestaurantDto::from));
    }

    @Cacheable(value = "restaurant", key = "#id")
    public RestaurantDto getById(UUID id) {
        return restaurantRepository.findById(id)
                .map(RestaurantDto::from)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));
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
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));
        r.setActive(!r.isActive());
        return RestaurantDto.from(restaurantRepository.save(r));
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
    public BranchDto updateOperatingHours(UUID branchId, java.util.Map<String, Object> hours) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new RuntimeException("Branch not found"));
        branch.setOperatingHours(hours);
        return BranchDto.from(branchRepository.save(branch));
    }
}

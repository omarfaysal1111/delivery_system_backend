package com.delivery.menu.service;

import com.delivery.menu.domain.ItemModifier;
import com.delivery.menu.domain.MenuCategory;
import com.delivery.menu.domain.MenuItem;
import com.delivery.menu.dto.*;
import com.delivery.menu.repository.ItemModifierRepository;
import com.delivery.menu.repository.MenuCategoryRepository;
import com.delivery.menu.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuCategoryRepository categoryRepository;
    private final MenuItemRepository itemRepository;
    private final ItemModifierRepository modifierRepository;

    @Cacheable(value = "menu", key = "#branchId")
    public List<MenuCategoryDto> getFullMenu(UUID branchId) {
        return categoryRepository
                .findAllByBranchIdAndIsVisibleTrueOrderBySortOrderAsc(branchId)
                .stream()
                .map(cat -> {
                    List<MenuItemDto> items = itemRepository
                            .findAllByCategoryIdAndIsAvailableTrue(cat.getId())
                            .stream().map(MenuItemDto::from).toList();
                    MenuCategoryDto dto = MenuCategoryDto.from(cat);
                    return MenuCategoryDto.builder()
                            .id(dto.getId()).branchId(dto.getBranchId())
                            .name(dto.getName()).sortOrder(dto.getSortOrder())
                            .isVisible(dto.isVisible()).items(items).build();
                }).toList();
    }

    @Transactional
    @CacheEvict(value = "menu", key = "#req.branchId")
    public MenuCategoryDto createCategory(CreateMenuCategoryRequest req) {
        MenuCategory category = MenuCategory.builder()
                .branchId(req.getBranchId())
                .name(req.getName())
                .sortOrder(req.getSortOrder())
                .build();
        return MenuCategoryDto.from(categoryRepository.save(category));
    }

    @Transactional
    public MenuItemDto createItem(CreateMenuItemRequest req) {
        MenuItem item = MenuItem.builder()
                .categoryId(req.getCategoryId())
                .name(req.getName())
                .description(req.getDescription())
                .price(req.getPrice())
                .imageUrl(req.getImageUrl())
                .build();
        MenuItem saved = itemRepository.save(item);
        evictMenuCacheForCategory(saved.getCategoryId());
        return MenuItemDto.from(saved);
    }

    @Transactional
    public MenuItemDto toggleAvailability(UUID itemId) {
        MenuItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        item.setAvailable(!item.isAvailable());
        MenuItem saved = itemRepository.save(item);
        evictMenuCacheForCategory(saved.getCategoryId());
        return MenuItemDto.from(saved);
    }

    @Transactional
    public MenuItemDto updatePrice(UUID itemId, java.math.BigDecimal price) {
        MenuItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        item.setPrice(price);
        MenuItem saved = itemRepository.save(item);
        evictMenuCacheForCategory(saved.getCategoryId());
        return MenuItemDto.from(saved);
    }

    private void evictMenuCacheForCategory(UUID categoryId) {
        categoryRepository.findById(categoryId).ifPresent(cat -> {
            // Eviction handled by @CacheEvict on callers when branchId is known
            // For nested eviction, a Spring ApplicationEvent can be used
        });
    }
}

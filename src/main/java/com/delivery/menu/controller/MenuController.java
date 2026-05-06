package com.delivery.menu.controller;

import com.delivery.menu.dto.*;
import com.delivery.menu.service.MenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @GetMapping("/branch/{branchId}")
    public ResponseEntity<List<MenuCategoryDto>> getFullMenu(@PathVariable UUID branchId) {
        return ResponseEntity.ok(menuService.getFullMenu(branchId));
    }

    @PostMapping("/categories")
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER', 'ADMIN')")
    public ResponseEntity<MenuCategoryDto> createCategory(
            @Valid @RequestBody CreateMenuCategoryRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(menuService.createCategory(req));
    }

    @PostMapping("/items")
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER', 'ADMIN')")
    public ResponseEntity<MenuItemDto> createItem(
            @Valid @RequestBody CreateMenuItemRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(menuService.createItem(req));
    }

    @PatchMapping("/items/{itemId}/toggle")
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER', 'ADMIN')")
    public ResponseEntity<MenuItemDto> toggleAvailability(@PathVariable UUID itemId) {
        return ResponseEntity.ok(menuService.toggleAvailability(itemId));
    }

    @PatchMapping("/items/{itemId}/price")
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER', 'ADMIN')")
    public ResponseEntity<MenuItemDto> updatePrice(
            @PathVariable UUID itemId,
            @RequestParam BigDecimal price) {
        return ResponseEntity.ok(menuService.updatePrice(itemId, price));
    }
}

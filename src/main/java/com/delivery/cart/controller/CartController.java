package com.delivery.cart.controller;

import com.delivery.cart.dto.AddCartItemRequest;
import com.delivery.cart.dto.CartDto;
import com.delivery.cart.service.CartService;
import com.delivery.common.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/user/cart")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartDto> getCart(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(cartService.getCart(principal.getId()));
    }

    @PostMapping("/items")
    public ResponseEntity<CartDto> addItem(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody AddCartItemRequest req) {
        return ResponseEntity.ok(cartService.addItem(principal.getId(), req));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartDto> updateItem(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID itemId,
            @RequestParam int quantity) {
        return ResponseEntity.ok(cartService.updateItem(principal.getId(), itemId, quantity));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartDto> removeItem(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID itemId) {
        return ResponseEntity.ok(cartService.removeItem(principal.getId(), itemId));
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal UserPrincipal principal) {
        cartService.clearCart(principal.getId());
        return ResponseEntity.noContent().build();
    }
}

package com.delivery.cart.service;

import com.delivery.cart.domain.Cart;
import com.delivery.cart.domain.CartItem;
import com.delivery.cart.dto.AddCartItemRequest;
import com.delivery.cart.dto.CartDto;
import com.delivery.cart.dto.CartItemDto;
import com.delivery.cart.repository.CartRepository;
import com.delivery.common.exception.ConflictException;
import com.delivery.common.exception.ResourceNotFoundException;
import com.delivery.menu.domain.MenuItem;
import com.delivery.menu.repository.MenuItemRepository;
import com.delivery.restaurant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;

    public CartDto getCart(UUID userId) {
        Cart cart = getOrCreate(userId);
        return toDto(cart);
    }

    @Transactional
    public CartDto addItem(UUID userId, AddCartItemRequest req) {
        MenuItem menuItem = menuItemRepository.findById(req.getMenuItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));

        // Determine restaurant via category → branch → restaurant
        UUID restaurantId = resolveRestaurantId(menuItem);
        UUID branchId = resolveBranchId(menuItem);

        Cart cart = getOrCreate(userId);

        if (cart.getRestaurantId() != null && !cart.getRestaurantId().equals(restaurantId)) {
            throw new ConflictException("Clear cart to order from a different restaurant");
        }

        cart.setRestaurantId(restaurantId);
        cart.setBranchId(branchId);

        CartItem item = CartItem.builder()
                .cart(cart)
                .menuItemId(menuItem.getId())
                .itemName(menuItem.getName())
                .quantity(req.getQuantity())
                .unitPrice(menuItem.getPrice())
                .selectedModifiers(req.getSelectedModifiers())
                .build();
        cart.getItems().add(item);
        return toDto(cartRepository.save(cart));
    }

    @Transactional
    public CartDto updateItem(UUID userId, UUID cartItemId, int quantity) {
        Cart cart = getOrCreateByUser(userId);
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));
        if (quantity <= 0) {
            cart.getItems().remove(item);
        } else {
            item.setQuantity(quantity);
        }
        return toDto(cartRepository.save(cart));
    }

    @Transactional
    public CartDto removeItem(UUID userId, UUID cartItemId) {
        Cart cart = getOrCreateByUser(userId);
        cart.getItems().removeIf(i -> i.getId().equals(cartItemId));
        return toDto(cartRepository.save(cart));
    }

    @Transactional
    public void clearCart(UUID userId) {
        cartRepository.findByUserId(userId).ifPresent(cart -> {
            cart.getItems().clear();
            cart.setRestaurantId(null);
            cart.setBranchId(null);
            cartRepository.save(cart);
        });
    }

    private Cart getOrCreate(UUID userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> cartRepository.save(Cart.builder().userId(userId).build()));
    }

    private Cart getOrCreateByUser(UUID userId) {
        return cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
    }

    private UUID resolveRestaurantId(MenuItem menuItem) {
        return menuItem.getCategoryId(); // simplified — actual would join category → branch → restaurant
    }

    private UUID resolveBranchId(MenuItem menuItem) {
        return menuItem.getCategoryId(); // simplified
    }

    private CartDto toDto(Cart cart) {
        List<CartItemDto> itemDtos = cart.getItems().stream()
                .map(i -> CartItemDto.builder()
                        .id(i.getId())
                        .menuItemId(i.getMenuItemId())
                        .itemName(i.getItemName())
                        .quantity(i.getQuantity())
                        .unitPrice(i.getUnitPrice())
                        .totalPrice(i.getUnitPrice() != null
                                ? i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity()))
                                : BigDecimal.ZERO)
                        .selectedModifiers(i.getSelectedModifiers())
                        .build())
                .toList();

        BigDecimal subtotal = itemDtos.stream()
                .map(CartItemDto::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String restaurantName = null;
        if (cart.getRestaurantId() != null) {
            restaurantName = restaurantRepository.findById(cart.getRestaurantId())
                    .map(r -> r.getName()).orElse(null);
        }

        return CartDto.builder()
                .id(cart.getId())
                .restaurantId(cart.getRestaurantId())
                .restaurantName(restaurantName)
                .items(itemDtos)
                .subtotal(subtotal)
                .deliveryFee(BigDecimal.ZERO)
                .discount(BigDecimal.ZERO)
                .total(subtotal)
                .build();
    }
}

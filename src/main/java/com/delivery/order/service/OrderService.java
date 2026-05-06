package com.delivery.order.service;

import com.delivery.common.dto.PageResponse;
import com.delivery.common.exception.AuthException;
import com.delivery.menu.domain.MenuItem;
import com.delivery.menu.repository.MenuItemRepository;
import com.delivery.order.domain.Order;
import com.delivery.order.domain.OrderItem;
import com.delivery.order.domain.OrderStatus;
import com.delivery.order.dto.OrderDto;
import com.delivery.order.dto.PlaceOrderRequest;
import com.delivery.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final MenuItemRepository menuItemRepository;
    private final OrderStatusNotificationService notificationService;

    @Transactional
    public OrderDto placeOrder(UUID customerId, PlaceOrderRequest req) {
        Order order = Order.builder()
                .customerId(customerId)
                .branchId(req.getBranchId())
                .deliveryFee(req.getDeliveryFee())
                .build();

        BigDecimal subtotal = BigDecimal.ZERO;
        for (var itemReq : req.getItems()) {
            MenuItem menuItem = menuItemRepository.findById(itemReq.getMenuItemId())
                    .orElseThrow(() -> new RuntimeException("Menu item not found: " + itemReq.getMenuItemId()));
            if (!menuItem.isAvailable()) {
                throw new RuntimeException("Item not available: " + menuItem.getName());
            }
            BigDecimal lineTotal = menuItem.getPrice().multiply(BigDecimal.valueOf(itemReq.getQty()));
            subtotal = subtotal.add(lineTotal);

            OrderItem oi = OrderItem.builder()
                    .order(order)
                    .menuItemId(menuItem.getId())
                    .itemName(menuItem.getName())
                    .qty(itemReq.getQty())
                    .unitPrice(menuItem.getPrice())
                    .modifiers(itemReq.getModifiers())
                    .build();
            order.getItems().add(oi);
        }

        order.setSubtotal(subtotal);
        order.setTotal(subtotal.add(req.getDeliveryFee()));

        Order saved = orderRepository.save(order);
        return OrderDto.from(saved);
    }

    @Transactional
    public OrderDto cancelOrder(UUID orderId, UUID requesterId) {
        Order order = findOrder(orderId);
        if (!order.getCustomerId().equals(requesterId)) {
            throw new AuthException("Not authorized to cancel this order");
        }
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new RuntimeException("Order cannot be cancelled at current status: " + order.getStatus());
        }
        order.setStatus(OrderStatus.CANCELLED);
        Order saved = orderRepository.save(order);
        notificationService.notifyStatusChange(orderId, OrderStatus.CANCELLED);
        return OrderDto.from(saved);
    }

    @Transactional
    public OrderDto updateStatus(UUID orderId, OrderStatus newStatus) {
        Order order = findOrder(orderId);
        order.setStatus(newStatus);
        Order saved = orderRepository.save(order);
        notificationService.notifyStatusChange(orderId, newStatus);
        return OrderDto.from(saved);
    }

    public OrderDto getOrder(UUID orderId) {
        return OrderDto.from(findOrder(orderId));
    }

    public OrderStatus getStatus(UUID orderId) {
        return findOrder(orderId).getStatus();
    }

    public PageResponse<OrderDto> getCustomerOrders(UUID customerId, Pageable pageable) {
        return PageResponse.of(
                orderRepository.findAllByCustomerIdOrderByCreatedAtDesc(customerId, pageable)
                        .map(OrderDto::from));
    }

    public PageResponse<OrderDto> getBranchOrders(UUID branchId, Pageable pageable) {
        return PageResponse.of(
                orderRepository.findAllByBranchIdOrderByCreatedAtDesc(branchId, pageable)
                        .map(OrderDto::from));
    }

    private Order findOrder(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found: " + id));
    }
}

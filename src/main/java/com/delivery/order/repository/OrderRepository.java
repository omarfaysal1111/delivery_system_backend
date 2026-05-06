package com.delivery.order.repository;

import com.delivery.order.domain.Order;
import com.delivery.order.domain.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    Page<Order> findAllByCustomerIdOrderByCreatedAtDesc(UUID customerId, Pageable pageable);
    Page<Order> findAllByBranchIdOrderByCreatedAtDesc(UUID branchId, Pageable pageable);
    List<Order> findAllByBranchIdAndStatus(UUID branchId, OrderStatus status);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt BETWEEN :from AND :to")
    long countByCreatedAtBetween(LocalDateTime from, LocalDateTime to);
}

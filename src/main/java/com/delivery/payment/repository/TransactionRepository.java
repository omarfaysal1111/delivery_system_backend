package com.delivery.payment.repository;

import com.delivery.payment.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    Optional<Transaction> findByOrderId(UUID orderId);
    Optional<Transaction> findByStripePaymentIntentId(String intentId);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.status = 'SUCCEEDED' AND t.createdAt BETWEEN :from AND :to")
    BigDecimal sumRevenueByDateRange(LocalDateTime from, LocalDateTime to);
}

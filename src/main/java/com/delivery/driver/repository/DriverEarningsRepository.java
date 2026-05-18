package com.delivery.driver.repository;

import com.delivery.driver.domain.DriverEarnings;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface DriverEarningsRepository extends JpaRepository<DriverEarnings, UUID> {

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM DriverEarnings e " +
           "WHERE e.driverId = :driverId AND e.type <> 'withdrawal' " +
           "AND e.createdAt BETWEEN :from AND :to")
    BigDecimal sumByDriverAndDateRange(UUID driverId, LocalDateTime from, LocalDateTime to);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM DriverEarnings e " +
           "WHERE e.driverId = :driverId AND e.type <> 'withdrawal'")
    BigDecimal totalEarningsByDriver(UUID driverId);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM DriverEarnings e " +
           "WHERE e.driverId = :driverId AND e.type = 'withdrawal'")
    BigDecimal totalWithdrawalsByDriver(UUID driverId);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM DriverEarnings e " +
           "WHERE e.driverId = :driverId AND e.type = :type AND e.createdAt BETWEEN :from AND :to")
    BigDecimal sumByDriverTypeAndDateRange(UUID driverId, String type, LocalDateTime from, LocalDateTime to);

    List<DriverEarnings> findByDriverIdOrderByCreatedAtDesc(UUID driverId, Pageable pageable);
}

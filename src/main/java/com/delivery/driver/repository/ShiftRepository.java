package com.delivery.driver.repository;

import com.delivery.driver.domain.Shift;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ShiftRepository extends JpaRepository<Shift, UUID> {
    List<Shift> findAllByIsActiveTrueAndDateGreaterThanEqualOrderByDateAsc(LocalDate from);
}

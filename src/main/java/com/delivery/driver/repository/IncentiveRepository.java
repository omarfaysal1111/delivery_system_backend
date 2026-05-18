package com.delivery.driver.repository;

import com.delivery.driver.domain.Incentive;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface IncentiveRepository extends JpaRepository<Incentive, UUID> {
    List<Incentive> findAllByIsActiveTrue();
}

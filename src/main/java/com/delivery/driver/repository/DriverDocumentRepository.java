package com.delivery.driver.repository;

import com.delivery.driver.domain.DriverDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DriverDocumentRepository extends JpaRepository<DriverDocument, UUID> {
    List<DriverDocument> findAllByDriverId(UUID driverId);
}

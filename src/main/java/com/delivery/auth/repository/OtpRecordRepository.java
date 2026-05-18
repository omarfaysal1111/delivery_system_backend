package com.delivery.auth.repository;

import com.delivery.auth.domain.OtpRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface OtpRecordRepository extends JpaRepository<OtpRecord, UUID> {
    Optional<OtpRecord> findTopByPhoneAndUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
            String phone, LocalDateTime now);
}

package com.delivery.notification.repository;

import com.delivery.notification.domain.FcmToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FcmTokenRepository extends JpaRepository<FcmToken, UUID> {
    List<FcmToken> findAllByUserId(UUID userId);
    void deleteByUserIdAndToken(UUID userId, String token);
    boolean existsByUserIdAndToken(UUID userId, String token);
}

package com.delivery.notification.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "fcm_tokens",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "token"}))
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class FcmToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String token;

    private String platform;

    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    void onUpdate() { updatedAt = LocalDateTime.now(); }
}

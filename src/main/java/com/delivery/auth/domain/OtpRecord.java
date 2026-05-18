package com.delivery.auth.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "otp_records")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class OtpRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String code;

    private LocalDateTime expiresAt;

    @Builder.Default
    private boolean used = false;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() { createdAt = LocalDateTime.now(); }
}

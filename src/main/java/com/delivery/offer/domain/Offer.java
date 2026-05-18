package com.delivery.offer.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "offers")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Offer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID restaurantId;

    private String title;
    private int discountPercent;

    @Column(precision = 10, scale = 2)
    private BigDecimal minOrderAmount;

    private String description;

    @Builder.Default
    private boolean isActive = true;

    private LocalDateTime expiresAt;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() { createdAt = LocalDateTime.now(); }
}

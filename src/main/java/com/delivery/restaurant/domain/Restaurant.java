package com.delivery.restaurant.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "restaurants")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID ownerId;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String slug;

    private String cuisineType;

    private String coverImageUrl;

    private String logoImageUrl;

    @Column(length = 1000)
    private String description;

    private Integer deliveryTimeMin;

    private Integer deliveryTimeMax;

    @Column(precision = 10, scale = 2)
    private BigDecimal deliveryFee;

    @Column(precision = 10, scale = 2)
    private BigDecimal minimumOrder;

    @Column(precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal commissionPct = BigDecimal.valueOf(15);

    @Builder.Default
    private boolean isActive = true;

    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() { createdAt = LocalDateTime.now(); updatedAt = createdAt; }

    @PreUpdate
    void onUpdate() { updatedAt = LocalDateTime.now(); }
}

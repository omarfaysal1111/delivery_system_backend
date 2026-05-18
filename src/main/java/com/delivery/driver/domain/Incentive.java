package com.delivery.driver.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "incentives")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Incentive {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String title;
    private String description;
    private int targetTrips;

    @Column(precision = 10, scale = 2)
    private BigDecimal bonusAmount;

    private LocalDateTime deadline;

    @Builder.Default
    private boolean isActive = true;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() { createdAt = LocalDateTime.now(); }
}

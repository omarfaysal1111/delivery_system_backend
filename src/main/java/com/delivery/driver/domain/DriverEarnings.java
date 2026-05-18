package com.delivery.driver.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "driver_earnings")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class DriverEarnings {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID driverId;

    private UUID orderId;
    private String type;

    @Column(precision = 10, scale = 2)
    private BigDecimal amount;

    private String description;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() { createdAt = LocalDateTime.now(); }
}

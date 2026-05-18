package com.delivery.driver.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "shifts")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Shift {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private int maxDrivers;

    @Builder.Default
    private boolean isActive = true;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() { createdAt = LocalDateTime.now(); }
}

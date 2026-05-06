package com.delivery.user.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "driver_profiles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID userId;

    private String vehicleType;
    private String licensePlate;

    @Builder.Default
    private Double avgRating = 0.0;

    @Builder.Default
    private boolean isOnline = false;

    private Double lastLat;
    private Double lastLng;
}

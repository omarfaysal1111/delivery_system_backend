package com.delivery.address.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_addresses")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class UserAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    private String label;
    private String fullAddress;
    private Double lat;
    private Double lng;
    private String city;
    private String neighborhood;

    @Builder.Default
    private boolean isDefault = false;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() { createdAt = LocalDateTime.now(); }
}

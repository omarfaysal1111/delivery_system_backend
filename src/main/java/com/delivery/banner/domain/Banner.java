package com.delivery.banner.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "banners")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Banner {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String imageUrl;
    private String title;
    private String subtitle;
    private String ctaText;
    private String discountText;
    private String deepLink;

    @Builder.Default
    private boolean isActive = true;

    private int sortOrder;

    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;

    @PrePersist
    void onCreate() { createdAt = LocalDateTime.now(); }
}

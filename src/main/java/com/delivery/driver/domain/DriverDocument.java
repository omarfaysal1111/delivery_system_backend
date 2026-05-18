package com.delivery.driver.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "driver_documents")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class DriverDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID driverId;

    private String type;
    private String fileUrl;

    @Builder.Default
    private String status = "pending";

    @Column(updatable = false)
    private LocalDateTime uploadedAt;

    @PrePersist
    void onPersist() { uploadedAt = LocalDateTime.now(); }
}
